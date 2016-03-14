# Changes

Changes made in this project include but not limit to:

- Renaming `ActivatableAbilities` to `ActivatableAbilityList`
- Re-organising the static fields and methods in `Auras`, `CardAuras`, `WeaponAuras` and `MinionAuras`
  to `Auras`, `AuraFilters`, `AuraTargetProviders` and `ActivatableAbilities` for better readability
  in Json files and naming consistency.
- Removing redundant methods in `EntitySelectors`.
- Renaming `BasicFilters` to `WorldEventFilters` and moving to `events` package.
- Deleting `WorldActionQueue` and `WorldActionProcessor`.

# Refactoring TODO list

- [x] Refactor classes in `abilities` package.
- [x] Refactor classes in `actions` package.
- [x] Refactor classes in `cards` package.
- [x] Refactor classes in `minions` package.
- [x] Refactor classes in `weapons` package.
- [ ] Refactor classes in `events` package.
- [ ] Refactor classes in the root package.

- [ ] Finish documenting the static fields and methods in `TargetlessActions.java`.
- [ ] Review every static `merge` method of interfaces to see if more flexible generic type is possible (See the `merge` method in `EntitySelector.java`).
- [ ] Combine `AuraFilter` and `AuraTargetProvider` to `TargetFilter`. (See the TODO comment in `AuraFilter.java`)
- [ ] Refactor the `PlayAction` and `PlayActionDef` framework.
- [ ] Refactor the `Minion`, `MinionProperties`, `MinionBody` framework.