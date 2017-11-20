Fastjson Codec
===================

This module adds support for encoding and decoding JSON via Fastjson.

Add `FastjsonEncoder` and/or `FastjsonDecoder` to your `Feign.Builder` like so:

```java
GitHub github = Feign.builder()
                     .encoder(new FastjsonEncoder())
                     .decoder(new FastjsonDecoder())
                     .target(GitHub.class, "https://api.github.com");
```

If you want to customize the `ParserConfig` and `SerializeConfig` that are used, provide it to the `FastjsonEncoder` and `FastjsonDecoder`:

```java
ParserConfig parserConfig = new ParserConfig();
//Custom the parserConfig...
SerializeConfig serializeConfig = new SerializeConfig();
//Custom the serializeConfig...

GitHub github = Feign.builder()
                     .encoder(new FastjsonEncoder(serializeConfig))
                     .decoder(new FastjsonDecoder(parserConfig))
                     .target(GitHub.class, "https://api.github.com");
```
