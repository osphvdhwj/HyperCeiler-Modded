# AVES+ Tools Provider Contract v1.0.0

Frozen interface between **HyperHand** (consumer) and **AVES+ Tools**
(producer).

## Identity

| Field | Value |
|-------|-------|
| Package | `com.avesplus.tools` |
| Authority | `content://com.avesplus.tools.provider` |
| Permission | `com.avesplus.tools.PROVIDER` (protectionLevel=signature) |
| Version | `provider.call("provider.version", null, null)` → `"1.0.0"` |

## Transport

`ContentProvider.call(Uri, String method, String arg, Bundle extras) → Bundle`

- `method` — tool name (`ocr`, `nsfw`, `provider.version`, …)
- `arg` — primary scalar (path, id, text) or `null`
- `extras` — additional named inputs
- reply Bundle contains a single key `"json"` whose value is the result envelope as a JSON string.

Long-running calls return immediately with `jobId` set; polling is
described below.

## Result envelope

```json
{
  "tool": "ocr",
  "version": "1.0.0",
  "ts": 1735483200123,
  "durationMs": 412,
  "ok": true,
  "data": { },
  "error": null,
  "jobId": null
}
error is a stable code when ok is false. Standard codes:

model_missing · file_not_found · unsupported_format ·
out_of_memory · cancelled · unauthorized

When error == "model_missing", data.install carries the exact
install command (e.g. pip install nudenet).

Auth

Provider MUST check Binder.getCallingUid() on every call:

Caller UID Policy
1000 (system_server) allow
provider's own UID allow
same signing key as provider allow
anything else SecurityException before any work

Auth handshake: provider.call("auth", null, extras) →
{allowed: bool, reason: string, uid: int}.

Long operations

Every call returns within 500 ms.

1. Provider allocates jobId, spawns work, returns {ok:true, jobId}.
2. Caller polls batch.status (jobId, null) every 500 ms.
3. Result fetched once via batch.result; entry evicted after retrieval.
4. batch.cancel cooperatively cancels.

Limits: 3 concurrent jobs per caller, jobId TTL 10 min, results
evicted 5 min after read.

Phase 1 tools (must ship)

Tool Input Data out
provider.version — "1.0.0"
provider.capabilities — [toolName, ...]
provider.ping — {latencyMs}
provider.auth — {allowed, reason, uid}
ocr path String
ocr.structured path {blocks:[{text,bbox}]}
nsfw path {score, labels:[String]}
image.embed path [Float]
image.similar {path, dir} [path]
translate {text, from, to} String
model.list — [{name, version, size, loaded}]
model.load name ok
model.memory — {freeMb, usedMb}
batch.* — see above

Phase 2 (HyperHand-only, no Aves dependency): audio, video, pdf,
archive, face.detect, face.embed, image.aesthetic, image.duplicate,
text., index., model.download.

Phase 3: everything else from the extended list.

Cross-cutting rules

1. Local only — no network by default. model.download is opt-in
   with a one-time disclosure screen at first call.
2. Cancellable — every tool has a jobId path.
3. Deterministic — same input → same output, or a version stamp
   explaining why not.
4. Offline-first — no crash if a model isn't installed.
5. Zero-copy for big payloads — files passed as paths. Byte streams
   only over a second call returning a ParcelFileDescriptor.
6. Uniform envelope — no tool-specific reply formats.

Versioning

provider.version returns the contract version (semver). Model
versions are separate (model.list). Breaking changes bump the major.
Consumers MUST feature-detect via provider.capabilities.

Reference consumer

library/hook/src/main/java/com/sevtinge/hyperceiler/hook/utils/bridge/AvesBridge.java
in the HyperHand tree is the reference implementation of this client.
Every call is wrapped so a missing provider is a silent no-op.
