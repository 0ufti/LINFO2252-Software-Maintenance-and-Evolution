# Architecture explained — source-reading guide (not a report)

## Core control flow (MVC)

```text
User clicks Swing button OR enters console command
        │
        ▼
Controller.performUiAction(...) / Main.execute(...)
        │ Controller translates action to domain operation
        ▼
LearningModel.startLesson / answer / changeFeatures / buyStats ...
        │ The Model changes progress/configuration and patches the UI registry
        ▼
UiStateLog.updateGroup("header"/"navigation"/"main"/"footer", elements)
        │ Does ID-based insert / change / remove; retains CURRENT state only
        ▼
LearningModel notifies ModelListener observers with immutable elements
        │
        ▼
SwingView draws on Swing EDT using these same UiElement objects
        └──── button clicked ──► Controller.performUiAction(button ID)

getStateAsLog() ──► UiStateLog.stateAsLog() ──► element.toLog() strings
```

### MVC boundaries

- **Model:** `LearningModel`, feature classes, question repository, game rules, progress and incremental UI specification. It does not import Swing.
- **View:** `SwingView`: layout, buttons, theme colours, rendering, Swing Event Dispatch Thread. It cannot decide whether an answer is correct or set a user as premium.
- **Controller:** the unmodified `ControllerInterface`, `Controller`, and console adapter `Main`. Requests are translated into model operations. It dispatches stable action IDs. This reduces coupling between UI layout and domain rules.

`UiElement` is an intentional *presentation model* owned by the Model. Its `id` is the stable action name, `kind` determines the component type, `text` is the current label, and `area` determines the UI region. The model updates the same data consumed by both the GUI and `getStateAsLog()`, reducing the risk of drift.

**Example log:** `button|nav.stats|Statistics|NAVIGATION`. A premium activation causes the same new element to appear in the GUI and the text state. `getStateAsLog()` is *not* a chronological audit trail. UI log entries are treated as a set for testing, as required by the lab instructions.

## Patterns: real role and trade-off

| Pattern / principle | Where | What problem it solves | Trade-off |
|---|---|---|---|
| **MVC** | `Controller`, `LearningModel`, `SwingView` | Separates domain logic from how users interact | UI descriptors are coordinated by the Model, an intentional choice for the lab's logs |
| **Observer** | `ModelListener`, `LearningModel.addListener`, `SwingView.onStateChanged` | View updates when state changes, regardless of UI/CLI trigger | Must schedule rendering on the Swing EDT; avoid memory leaks after closing View |
| **Command** | `FeatureChangeCommand` + `FeatureManager.execute()` | Represents activate/deactivate request as one atomic command | Only commands for feature changes, not a full command history/undo |
| **Strategy** | `LivesPolicy`, `FreeLivesPolicy`, `PremiumLivesPolicy` | Free/premium wrong-answer logic swaps at runtime | Small extra interface, justified by different game behaviour |
| **Repository** | `LessonRepository` | Centralizes lesson data, independent of Controller and UI | Currently in-memory; not a persistent database repository |
| **Immutable values** | `Question`, `UiElement`, `ProgressSummary`, `FeatureChangeCommand` | Prevents accidental state changes by View/Controller | Construct new values when data changes |
| **Incremental state materialization** | `UiStateLog.updateGroup` | Feature toggles patch current entries; no full log destruction | Each affected group is regenerated from current model state; Swing layout is rendered fresh |

Avoid claiming Singleton, Factory, Decorator, or Composite. This implementation does **not** use them as substantive design patterns.

## Atomic feature transition in detail

`Controller.activate(String[] deactivations, String[] activations)` is the instructor API.

1. Normalize each feature name to uppercase, collapse spaces.
2. Check every name exists in the **actual XML**, via `FeatureCatalogue`.
3. Reject unimplemented or impossible requests (e.g. mandatory feature deactivation, unknown features, same feature both enabled/disabled).
4. Copy active optional features and the selected subject to *candidate* values.
5. Apply the requested changes to the candidate, then apply domain constraints (Premium → Stats + Profile Skin, Flash Cards only with Premium, Theme parent dependencies, alternate subject selection).
6. Check purchase entitlements and invalid parent/child combinations.
7. **Only if all checks pass**, assign the candidate set and subject to the manager. No partial configuration is ever externally observable.
8. `LearningModel.changeFeatures` updates the affected screen if needed, updates current UI entries, and notifies Views.

**Example:** Start free on Physics and on the Shop screen; call `activate([], ["PREMIUM"])`:

- Premium becomes active, Stats and Profile Skin become active by implication.
- The `LivesPolicy` chosen on subsequent actions is `PremiumLivesPolicy` (unlimited).
- Chrome adds the Premium badge, replaces the lives text, and adds the Statistics navigation button.
- UI logs and Swing use the same entries; progress and current session persist.
- Deactivation removes Premium-only elements again. Purchased Statistics survives deactivation because it is independently owned via earned currency.

**Important limitation:** the feature catalogue reads the XML *names*, but `FeatureManager` enforces only the implemented subset's relationships manually. It does not provide a generic SAT solver or prove all arbitrary XML configurations valid. This is a conscious Mission 2 scope boundary and a useful improvement target.

## Incremental logging explained

`UiStateLog` stores a map from *region owner* (`header`, `navigation`, `main`, `footer`) to a map keyed by stable UI element ID. On update:

- Compute the new entries *for that region only*.
- Remove IDs no longer present.
- Replace an entry only if the value changed.
- Add IDs newly present.
- Preserve other regions; never accumulate historical events.

`LearningModel.refresh()` invokes `updateChrome()` and `updateMain()`. Those methods recalculate their small regions, while **`UiStateLog` applies incremental changes**; it does not throw away the entire log. This meets the lab's **current-state** and **runtime mutation** requirements without an event-sourcing framework.

One caveat to describe accurately in the report: the **Swing View itself repaints its layout from the latest state** after each notification. The requirement to avoid rebuilding all *logs* does not mean the Swing widget tree must also be patched in place. A future optimization could diff Swing components by ID too.

## Developer-facing design decisions

- `FeatureCatalogue` parses the original `feature_model.xml` via a hardened standard XML parser. It checks feature names at runtime; all other external dependencies are mocked or unnecessary.
- It uses a single user `Demo learner` and in-memory session state. No sensitive user data is collected.
- Only **supported** feature model names can be enabled. Requested leaderboard, audio, or screen-resolution capabilities currently return `2` to distinguish planned features from implemented ones.
- We preserve the spelling `GRAFICS` and `LEADERBROAD` because these are the names in the submitted XML; consider correcting the model and the Java references together after consultation.
- `FreeLivesPolicy` and `PremiumLivesPolicy` deliberately do not mutate or reset each other's historical life count. Changing tiers must not destroy earned progress.
- `Progress.recordCorrect` awards coins only for first-time completion. It tracks daily activity against an injected `Clock`, which allows deterministic time-sensitive tests.
- In the XML, **EASY/MEDIUM/HARD are mandatory**. Their selection is a lesson action, not runtime deactivation of a mandatory feature.
- The assignment's `enableUIView` and `disableUIView` signatures declare `boolean`, despite confusing Javadoc about integer return codes. The implementation correctly follows the **signatures**.

## Limitations you should openly acknowledge

- No disk persistence: exiting loses progress and purchases.
- Only one multiple-choice question for each subject × difficulty (9 total).
- No adaptive difficulty engine; the *features* adapt dynamically. For the final deliverable, a performance-based question selector would add more pedagogical adaptation.
- UI is intentionally basic; screenshots are not necessary for understanding the architecture.
- `FeatureManager` supports and enforces a **subset** of the XML; remaining declared features are not silently treated as completed.
- UML is simplified and should omit any unnecessary helper class if space is tight.
- JUnit 5 tests require Maven and its dependencies; the plain-JDK smoke harness is not a substitute for the JUnit suites.
