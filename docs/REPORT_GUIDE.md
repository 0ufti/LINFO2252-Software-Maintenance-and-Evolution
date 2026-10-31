# How to write YOUR Mission 2 report (guidance only, not a report)

Based on **M2-instructions.pdf** (dated October 22, 2026), **Lab2.pdf**, **Lab3.pdf**, **LINFO2252_Lab_sessions_testing.pdf**, and the group's existing M1 artifacts.

**Deadline given in the assignment:** Monday **2 November 2026, 23:55**. The report may contain **at most 6 pages excluding cover and pictures**. Submit the **project folder, PDF report, and FeatureIDE XML** together; upload the archive to Moodle and OneDrive, with an **"Everyone" download link pasted inside the PDF**. Confirm these instructions on Moodle before handing anything in.

This file is an outline of evidence and questions to answer in **your own words**, not text intended for copy-pasting into the submitted report. The Lab 1 instructions explicitly disallowed LLM-authored work for the first report/lexicon/feature model. Follow any additional AI-use policy for Mission 2 too.

## Suggested sections and what evidence to collect

### Cover (excluded from six-page maximum)

Include course `LINFO2252 — Software Maintenance and Evolution`, **SME-33**, author names, Mission 2, date. Use the authorship data from the original M1 report; do not fabricate signatures. Add the OneDrive link after uploading the final archive.

### Part I — updated feature model (approx. 1–2 pages of prose + standalone readable diagram)

1. **Updated feature diagram (FeatureIDE)**: open your original model in FeatureIDE, incorporate the feedback given on M1, and ensure the diagram fits **one A4 page** at readable size. Keep a separate `feature model.xml` export as required.
2. **Lexicon**: define terms whose meaning is not obvious: Learning Subject, Level, Subject/Theme, Lives, Premium, Economy, Stats, Streak, Daily Goal. Refine your previous definitions based on what the application actually implements.
3. **Feature rationales**: why ALT for subject selection; why easy/medium/hard are mandatory; why Premium differs from the free economy; why Shop and runtime optional features make sense; what a user actually experiences after activation.
4. **Review a mismatch before reporting**: your M1 PDF claims `Premium → Stats` and `Premium → Profile Skin`, but the supplied FeatureIDE XML has **no `<constraints>` block**. The code enforces the implications; consider adding them to your **own** FeatureIDE model after reviewing the course feedback. The original XML was kept intact in the prototype for traceability.
5. **Typos in original XML**: `GRAFICS`, `LEADERBROAD`, `LIMITED LIVE`, `INFINITE LIVE`. If you correct them, update Java code and tests consistently.

Do **not** claim that the feature model or the application already implements a leaderboard/audio functionality; the current prototype explicitly rejects those runtime activations.

### Part II — system architecture and tests (remaining ~4–5 pages)

**Simplified class diagram**

- Open/edit `docs/CLASS_DIAGRAM.puml` in PlantUML, export a legible figure.
- Keep key classes for **MVC**, **Observer**, **Command**, **Strategy**, and **incremental logging**. Drop peripheral entities if the figure becomes unreadable.
- Explain relationships that matter: `Controller implements ControllerInterface`, `LearningModel owns FeatureManager`, `SwingView implements ModelListener`, `LivesPolicy` is implemented by free and premium strategies.

**MVC explanation**

- Model: `LearningModel`, `FeatureManager`, `Progress`, `LessonRepository`, `UiStateLog`.
- View: `SwingView`, Swing buttons and labels generated from the same `UiElement` descriptors as the logs.
- Controller: `Main` parses developer commands; `Controller` maps actions to model calls and implements the unmodified course interface.
- Trace a concrete example, such as selecting Physics, starting Easy, answering, then enabling Premium.
- Name the implemented patterns accurately; no need to invent extra patterns.

**Log representation**

- Describe `kind|stable-id|text|region` and list several **actual** entries from the terminal's `status` command. For example: `button|nav.shop|Shop|NAVIGATION`.
- Explain why the state log represents **current UI** rather than a chronological event history. Explain why tests compare **sets**, not sequence ordering.
- Show how a feature adds/removes button/badge entries. Note that the UI and logs are generated from the same `UiElement` objects.
- Explain the nuance: the **log store is incrementally patched**; the Swing layout is repainted based on the current state.

**Feature (de)activation protocol**

- Pick a real transition: Free → Premium or Premium + Flash Card → Free.
- Document `Controller.activate()` (course signature); `FeatureChangeCommand`; `FeatureManager.execute()`'s validation/working copy/commit; update of `UiStateLog`, notification and redraw.
- Explain that the model accepts only declared and **implemented** features. Unknown name returns 1, declared unimplemented returns 2, invalid configuration returns 3, insufficient entitlement returns 4.
- Describe the inter-feature implications from M1: Premium → Stats + Profile Skin. Explain why purchased Stats can remain enabled without Premium. Explain the free/premium Strategy and what changes immediately.
- Include one snapshot of relevant log lines before and after activation (save console output yourself).

**Tests**

- Use **JUnit 5**, as requested. Include a discussion of **one unit test suite** (suggestion: `ControllerTest.java`) and reproduce/explain **two or three tests** chosen by your group: `rejectsUnknownFeatureWithoutPartialActivation`, `premiumAutomaticallyEnablesStatsAndSkin`, `cannotDeactivateLastSubjectOrMandatoryDifficulty`.
- Explain unit-test initial-state isolation (`@BeforeEach`) and assertions; do not merely say "tests don't crash".
- The `ConfigurationTest.java` file has **12 fixed valid feature configurations**, covering **three subject choices**, **free vs Premium**, **Streaks**, **Daily Goal**, **Theme**, and **Flash Card**. Explain how the set samples feature interactions. Explain that some features (Premium implies Stats/Skin) cannot legally be independently disabled while Premium is active.
- The `SystemAdaptationTest.java` file has **3 usage scenarios** with runtime feature changes. A strong one to describe: answer incorrectly as a free player (life consumed), activate Premium mid-session (no life consumed), deactivate Premium (life consumed again).
- The `LogTest.java` file has **2 consistency tests**: current-state removal and activation-order independence.
- Record actual `mvn test` results in your report **after running them yourself**. The plain-Java smoke runner exercises important actions but **does not replace the JUnit tests**.

## Screenshot / manual observations checklist

- [ ] Run CLI `status` initially, copy some **actual** UI log lines.
- [ ] Activate Premium, observe unlimited lives + Stats navigation and Premium badge.
- [ ] Deactivate Premium, observe removal; show no stale UI logs.
- [ ] Activate two independent features in opposite orders; verify the final log *sets* match.
- [ ] Run `mvn test`, save the number of tests and any failures (and fix failures first).
- [ ] Open Swing UI, select all 3 courses and answer a question.
- [ ] Use `buy stats` after earning sufficient coins; show premium feature access without Premium package.
- [ ] Update FeatureIDE model after considering M1 feedback; recheck model constraints.
- [ ] Review diagrams and test claims for accuracy and legibility on A4.

## Suggested report space budget (only a guide)

- Cover: separate, not counted.
- Updated feature model, lexicon, rationales: ~1.5 pages of prose; model on its own figure page.
- Class diagram and MVC: ~1–1.5 pages of prose plus figure.
- Logs and activation walkthrough: ~1 page.
- Testing strategy / sample tests / limitations: ~1.5–2 pages.

The mission explicitly says the **project should be a complete prototype**, not every planned feature. It gives examples of acceptable scope: **one lesson per course, one premium functionality, and a basic UI**; the rest can be implemented later.

## Packaging for Moodle

The prototype is distributed separately from the student-authored report. For the actual submission, construct a zip called **`M2 Group-SME33.zip`** containing at least:

```text
M2_Group-SME33_Report.pdf      # YOU write this; not supplied here
feature model.xml              # your checked / updated FeatureIDE model
SmartLearning_M2_SME33/       # the project directory (source, tests, resources, docs)
```

Upload the final archive to OneDrive and choose "Everyone" / download permissions; insert that link in the PDF. Do not include unnecessary `build/`, `target/`, `.git/`, or IDE caches in the final project folder.
