# NoteBlockAPI (Minestom)

Play Open Note Block Studio (`.nbs`) songs on [Minestom](https://minestom.net/).

## Usage

```java
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import com.xxmicloxx.NoteBlockAPI.model.Song;

Song song = NBSDecoder.parse(Path.of("song.nbs"));
RadioSongPlayer.play(song, player);

// or:
RadioSongPlayer rsp = new RadioSongPlayer(song);
rsp.addPlayer(player).setPlaying(true);
```

On server shutdown: `NoteBlockAPI.shutdown()`.

Events: `NoteBlockAPI.getEventNode()`.

## Song players

| Class | Behavior |
|-------|----------|
| `RadioSongPlayer` | All listeners, any location |
| `PositionSongPlayer` | Fixed `Instance` + `Pos` + range |
| `EntitySongPlayer` | Follows an entity + range |
| `NoteBlockSongPlayer` | Note block + range |

Stereo panning from the NBS file is applied automatically. Use `setFakeStereo(true)` for dual-source mono songs. Use `setEnable10Octave(true)` with a client instruments resource pack for notes outside the vanilla 2-octave range.

## Adding to Your Project
The latest NoteBlockAPI-minestom release is available as a dependency [here](https://maven.hapily.me/#/releases/com/xxmicloxx/NoteBlockAPI)

Requires Java 25+.
