# Brazier

This module acts as the game simulator for the AI module [AlphaHearth](../AlphaHearth). Most of the work comes from [Kelemen](https://github.com/kelemen)'s original version of [Brazier](https://github.com/HearthSim/Brazier), but great changes have been made for a better using experience.

## TO-DO List

More works are remained to be done in this module, which include:

- [ ] Add `clone` methods for classes like `Minion`, `Card`, `Player` and `World`, which will be used by the AI to copy a given game state (deep-copy a `World`).
- [ ] Remove the `undo` framework from the whole project, which brings unnecessary memory cost to the simulator.
- [ ] `Ability` is now only used to add aura to characters and spell power. Consider defining minion's `Aura` in Json files to replace `Ability`.
- [ ] Combine `AuraFilter` and `AuraTargetProvider` together, and extend it to use for other mechanics, such as renaming it to `TargetFilter` or `TargetProvider`.
- [ ] Several methods in `ActionUtils` with `World` as its parameter only use its `RandomProvider`. Consider using `RandomProvider` as parameter directly.
- [ ] Refactor the strange `PlayAction` framework, which now consists of `PlayActionDef`, `PlayActionRequirement` and `PlayAction`.
- [ ] Add exception framework to the project, which throws exception when invalid action is requested, e.g. `BoardFullException`.
