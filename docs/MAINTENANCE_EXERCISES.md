# Five practical maintenance exercises

These are not features already completed. They are **recommended exercises for the student developers** to demonstrate that they own and understand the prototype.

## 1. Add a new lesson question safely (easy)

**Change:** turn each `(Subject, Difficulty)` combination into **three questions** instead of one. Currently `LessonRepository` maps one difficulty to one `Question`. You will need a list, a current question index, and a clear completion condition. Preserve the existing `Question` model where possible.

**Tests:** ask two questions in sequence; progress increments once only when the lesson is complete; repeat lesson without farming unlimited currency.

**Maintenance lesson:** data model evolution, small refactoring, regression tests.

## 2. Add a performance-adaptive learning path (medium)

**Change:** after repeated wrong answers, recommend Easy before Medium; after consistently correct answers, suggest the next level. Consider a `DifficultyRecommendationStrategy` based on recent `Progress` history. Keep the `Difficulty` feature itself always available in XML.

**Tests:** wrong answers lead to a lower recommended difficulty, correct streak leads to a higher one; switching subjects uses the correct subject's progress.

**Maintenance lesson:** keep adaptive policy separate from the view and from lesson content.

## 3. Add a new premium functionality (medium)

**Change:** implement one of your own feature-model additions, such as `HINTS` (not present in supplied XML) or expand `FLASH CARD` to all levels. First edit the XML in **FeatureIDE**, then update `FeatureManager`, model, UI specification, controller action IDs, and relevant tests.

**Tests:** feature off ⇒ no new controls/logs; feature on ⇒ controls/logs present; disabling while currently displayed navigates safely away; invalid activation is atomic.

**Maintenance lesson:** feature traceability across model/configuration/controller/view/tests.

## 4. Extract the feature dependencies into reusable rules (medium-hard)

**Change:** `FeatureManager` currently has manually coded rules for Premium implications and setting parents. Introduce small rule objects (e.g. `FeatureRule`) or a dependency graph. Preserve transactional validation and all existing return codes.

**Tests:** independently activated features are order-invariant, Premium/Flash/Stats/Theme dependencies still work, last subject cannot be deactivated.

**Maintenance lesson:** refactor conditional logic without changing public behaviour.

## 5. Make progress persistent (medium-hard)

**Change:** introduce a `ProgressRepository` that reads/writes JSON snapshots (or another simple file format). Keep persistence separate from the View. Decide how to restore dates, purchases, lesson completion, and conflicting feature configurations.

**Tests:** save & reload keeps completed lesson count, coins, purchased statistics, and lives; invalid data produces a meaningful error; no partial restoration.

**Maintenance lesson:** boundaries, data migration, error handling, testability.

## Code review questions to ask each other

- Which class is allowed to change the coins? Why isn't SwingView allowed to do it?
- What breaks if feature names in the XML are renamed but the Java constants aren't updated?
- Why does `FeatureManager.execute` validate a copy before committing anything?
- What happens if a user switches course while a question is visible?
- Why can a free player use Statistics without Premium, but not Flash Cards?
- Is regenerating the Swing widget tree the same as regenerating the complete logs?
- Which patterns are actually present, and which were deliberately not used?
- Which public APIs must remain stable for the professor's external controller integration?
- What additional tests should be written before each of the exercises above?
