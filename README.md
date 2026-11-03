# Shaderboard
**Shaderboard** lets vanilla resource-pack post-effect shaders read client-side scoreboard values through **named bindings**.

A binding is declared inside a post-effect's JSON file (the same file that defines its passes). This mod turns each binding into a real GLSL uniform block with a GPU buffer, and rewrites those buffers with live scoreboard values every frame. Shaders can then use those values however they like.

> **Note:** This mod is for vanilla `/posteffect` command shaders. It does not target shaders from mods like [Iris](https://github.com/IrisShaders/Iris) and [Optifine](https://optifine.net/).

---

## Declaring bindings
Add a `"shaderboard:bindings"` array at the root of a [post-effect JSON](https://minecraft.wiki/w/Shader#Post-effect_file_format), next to `"targets"` and `"passes"`. Here is an example:
```json
{
	"targets": { ... },
	"passes": [ ... ],

	"shaderboard:bindings":
	[
		{
			"uniform_name": "u_TopScore",
			"type": "shaderboard:scoreboard_objective",
			"objective": "example_1",
			"target": { "type": "top_player" }
		},
		{
			"uniform_name": "u_MyScore",
			"type": "shaderboard:scoreboard_objective",
			"objective": "example_1",
			"target": { "type": "local_player" }
		},
		{
			"uniform_name": "u_SteveScore",
			"type": "shaderboard:scoreboard_objective",
			"objective": "example_1",
			"target": { "type": "player", "name": "Steve" }
		},
		{
			"uniform_name": "u_SortedScoresAZ",
			"type": "shaderboard:scoreboard_objective",
			"objective": "example_1",
			"target":
			{
				"type": "array",
				"count": 16,
				"include_hidden": true,
				"sort": [
					{ "by": "name", "order": "ascending" }
				],
				"offset": 0
			}
		}
	]
}
```

**Where:**
- `uniform_name` - Name of the GLSL uniform block a binding creates.
- `type` - Binding type. I.e. the type of information a binding exposes.

**When** `type` is `shaderboard:scoreboard_objective`**:**
- `objective` - Scoreboard objective name.
- `target` - Defines which score of an objective to bind.
	- `type` - The type of target to bind to; local player's score (`local_player`), a specific player's score (`player`), the top score (`top_player`), or a fixed-size array of scores (`array`).
	- `name` - When `type` is `player`, this additional field defines "which player".
	- `include_hidden` - When `type` is `top_player` or `array`; Whether to include hidden scores (whose names start with `#`). Defaults to `false`.
	- `count` - Required for `array`: fixed array length, from `1` to `1024`.
	- `sort` - When `type` is `array`; optional array of sort keys. Each entry requires `by` (`name` or `value`) and `order` (`ascending` or `descending`).
	- `offset` - When `type` is `top_player` or `array`; number of sorted scores to skip. Defaults to `0`.

---

## Reading a binding's value in GLSL

Each binding's `uniform_name` becomes a `layout(std140)` uniform block. Its contents are defined by the binding's **value type**. Scalar scoreboard targets produce a single `int`:

```glsl
layout(std140) uniform u_TopScore { int topScore; };
layout(std140) uniform u_SortedScoresAZ { int sortedScores[16]; };

void main()
{
	//base output color
	fragColor = texture(InSampler, texCoord);

	//...use 'topScore' and 'sortedScores' however you'd like here...
}
```

## Quick demo
This mod includes an example `/posteffect` of ID `shaderboard:example_1`. You may try it out using the following commands in order:
```txt
/posteffect add @p shaderboard:example_1

/scoreboard objectives add example_1 dummy "Example 1"
/scoreboard objectives setdisplay sidebar example_1

/scoreboard players set @p example_1 123
/scoreboard players set Steve example_1 456
```
This example shader reads scoreboard values from the `example_1` objective and renders them on-screen as binary stripes.
