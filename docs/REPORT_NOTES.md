# Notes for writing your own Mission 2 report

Do NOT submit this text as a report. These are points to verify and explain yourself.

The M2 instructions request **maximum 6 pages (excluding cover page and pictures)**, an updated feature model, a design section and tests. The ZIP should contain your PDF report, a FeatureIDE file named `feature model.xml`, and the full project folder. The OneDrive public share link must be in the report.

## Feature model section

- Update the lexicon from M1 using any feedback, notably for feature names that are ambiguous.
- Recheck the M1 rationales and add explanations where a feature choice isn't obvious.
- Include a readable FeatureIDE diagram on one printable A4 page.
- Important: the provided M1 XML does not declare `PREMIUM -> STATS` and `PREMIUM -> PROFILE SKIN` constraints even though the M1 report discusses them. Decide how to represent them and update your XML yourself.
- Be transparent about prototype vs planned features. `LEADERBROAD` and sound/resolution are still not implemented.

## Design section

- Draw a **simplified** class diagram. The `.puml` file in this folder is just a starting point.
- Explain MVC: `Main` and `BasicView` receive input; `Controller` delegates actions; `LearningModel` owns game rules and state.
- Explain patterns with concrete lines of code, not definitions only:
  - `LivesPolicy` / `FreeLives` / `PremiumLives`: Strategy.
  - `FeatureCommand` and `FeatureManager.change`: Command + transaction on a copy.
  - `LearningModel.addListener` / `refresh` / `BasicView`: Observer.
  - `QuestionBank`: separation of course data from gameplay.
- Explain the log structure: `type|id|text|place`, for example `button|shop|Shop|NAV`.
- Explain that `getStateAsLog` represents the current UI and not a chronological history.
- Show a real runtime scenario, e.g. with `activate premium`, `start easy`, `answer 2`, `deactivate premium`, `logs`, and relate each change to the Model and the UI description.
- Explain how a failed multi-feature activation leaves the previous feature state unchanged.

## Testing section

- Choose one class for the required **unit-test suite**, describe 2 or 3 individual assertions and their expected behaviour. `QuestionTest` or `ControllerTest` works.
- Give representative configurations that cover free/Premium, subjects, settings, daily goal and locked Statistics; explain why you chose them.
- Describe a **system test** that actually switches feature configuration at runtime while the app is being used. `premiumChangesLivesDuringExecution` is the obvious example.
- Explain log consistency (Premium badge and Statistics navigation are added/removed, independent activation order doesn't matter).
- Run the official JUnit tests using `mvn test` on your own computer; do not claim to have run Maven if you haven't.

## Honest limitations

- Very basic Swing UI, terminal-first prototype.
- Hard-coded questions, only one per difficulty and subject.
- XML names copied manually rather than automatically parsed.
- No persistence or server, several future features missing.
- Some design trade-offs still to refactor (many responsibilities in LearningModel, String IDs, no array copy in FeatureCommand).

## Submission

The **final** submission archive should look like this (not the provided development ZIP):

```
M2 Group-SME33.zip
    M2_Group-SME33_Report.pdf
    feature model.xml
    SmartLearning_WIP_M2/
        src/
        tests/
        docs/
        pom.xml
        run.bat
        run.sh
        README.md
```

Do not include `out/`, compiled `.class` files, `target/` or `.git/`.
