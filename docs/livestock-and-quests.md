# Livestock, quests and graphics

The Java entities, service and repository remain authoritative. The frontend only renders sprites, alerts and server-provided quest progress.

## Running the updated version

Stop the old `run.ps1` terminal with Ctrl+C, then run `./run.ps1` again. Reload the browser with Ctrl+F5. This update changes Java classes as well as frontend files; a browser refresh alone cannot load the new quest API.

The active backend is still SQLite. On startup, `database/schema.sql` adds `quest_progress` and `quest_claims` with `CREATE TABLE IF NOT EXISTS`; existing currency, plots, animals and the first claimed reward are preserved. Historical planting, watering, feeding and sales counters begin at zero for old saves. Existing harvest totals, expansion and owned habitats count immediately. No MySQL connection is introduced by this update.

## Quest model

`Entity/Quest.java` defines a quest. `Service/QuestCatalog.java` contains 14 definitions. `GameState` counts successful actions and grants each reward once. `GameService` exposes `quests` in state and accepts `claimQuest` with a `quest` id. `GameSave` includes counters and claimed ids; the repository saves them in the same transaction as the wallet.

Quests cover harvesting, planting, watering, level 3 flowers, feeding each species, eggs, pork, beef, livestock sales, expansion, all three habitats and a new cottage. The legacy `claim` action remains compatible with the first harvest quest.

Click an animal's ! to open its relevant quests and feed/collection action. The markers are also keyboard-focusable buttons. The mail ! means at least one quest reward can be claimed. M opens all quests.

## Graphics

Settings includes Auto, Low and High. The preference is stored locally for each browser/device. Low uses 20 frames per second and pixel ratio 1; High uses 30 frames per second with pixel ratio capped at 1.5. Auto starts Low on devices reporting 4 or fewer logical cores and lowers quality when Canvas drawing repeatedly takes over 24 ms. Rendering pauses while a modal is open or the tab is hidden. Gameplay timers remain on the Java server. System reduced-motion preference is respected.

Terrain and flower art are cached. A mocked Canvas command count with the same fixture decreased from 11,781 to 3,245 per warm frame (72% fewer calls). This measures drawing work, not a guaranteed FPS on every device.

Vietnamese headings use Arial/Segoe UI with room for stacked accents. Tool icons and cursors now resemble the turquoise watering can and grey/orange shovel references.

## Sprite provenance

`frontend/assets/livestock-atlas.png` was created using the built-in **imagegen** tool from the user's rooster, cow and pig reference images. It contains three equal 724px-wide cells, ordered rooster, cow, pig, with actual alpha transparency. Canvas and shop cards use the same image. No external runtime or image service is required while playing.

Final prompt:

> Create a transparent PNG game sprite atlas inspired by these three reference animals. Exactly three isolated full-body farm animals, arranged in a single horizontal row in three equal-width cells: LEFT the reference brown rooster with red comb, golden legs, large curved brown tail facing left; CENTER a black-and-white Holstein cow with four legs and small horns facing right; RIGHT a pale pink pig with upright ears, round snout and four hooves facing left. Preserve their identifying shapes and colors. Harmonize them as polished illustrated sprites for a colorful isometric farm game, subtle three-quarter view from above for cow/pig, readable silhouettes at small size. Each animal centered in its own cell with feet on the same baseline, generous transparent padding; no animal crosses its cell, no ground, no background, no shadows, no labels or text. One row only, no extra animals. True alpha transparency.

## Validation

Run `./run.ps1 -Test` for domain, quest, SQLite reload/rollback and HTTP tests. Browser verification covered feeding from an alert, progress changes, Vietnamese seed names, livestock cards and graphics settings at 16:9 and 16:10. Geometry checks also covered 800x600, expanded/unexpanded land, day/night and both quality levels.
