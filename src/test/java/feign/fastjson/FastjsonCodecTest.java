package feign.fastjson;



import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import feign.RequestTemplate;
import feign.Response;

import static feign.Util.UTF_8;
import static feign.assertj.FeignAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.alibaba.fastjson.TypeReference;

public class FastjsonCodecTest {

  private String zonesJson = ""//
                             + "[\n"//
                             + "  {\n"//
                             + "    \"name\": \"denominator.io.\"\n"//
                             + "  },\n"//
                             + "  {\n"//
                             + "    \"name\": \"denominator.io.\",\n"//
                             + "    \"id\": \"ABCD\"\n"//
                             + "  }\n"//
                             + "]\n";

  @Test
  public void encodesMapObjectNumericalValuesAsInteger() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("foo", 1);

    RequestTemplate template = new RequestTemplate();
    new FastjsonEncoder().encode(map, map.getClass(), template);

    assertThat(template).hasBody("{\"foo\":1}");
  }

  @Test
  public void encodesFormParams() throws Exception {
    Map<String, Object> form = new LinkedHashMap<String, Object>();
    form.put("foo", 1);
    form.put("bar", Arrays.asList(2, 3));

    RequestTemplate template = new RequestTemplate();
    new FastjsonEncoder().encode(form, new TypeReference<Map<String, ?>>() {
    }.getType(), template);

    assertThat(template).hasBody("{\"foo\":1,\"bar\":[2,3]}");
  }

  @Test
  public void decodes() throws Exception {
    List<Zone> zones = new LinkedList<Zone>();
    zones.add(new Zone("denominator.io."));
    zones.add(new Zone("denominator.io.", "ABCD"));

    Response response = Response.builder()
                .status(200)
                .reason("OK")
                .headers(Collections.<String, Collection<String>>emptyMap())
                .body(zonesJson, UTF_8)
                .build();
    assertEquals(zones, new FastjsonDecoder().decode(response, new TypeReference<List<Zone>>() {
    }.getType()));
  }

  @Test
  public void nullBodyDecodesToNull() throws Exception {
    Response response = Response.builder()
            .status(204)
            .reason("OK")
            .headers(Collections.<String, Collection<String>>emptyMap())
            .build();
    assertNull(new FastjsonDecoder().decode(response, String.class));
  }

  @Test
  public void emptyBodyDecodesToNull() throws Exception {
    Response response = Response.builder()
            .status(204)
            .reason("OK")
            .headers(Collections.<String, Collection<String>>emptyMap())
            .body(new byte[0])
            .build();
    assertNull(new FastjsonDecoder().decode(response, String.class));
  }



  public static class Zone extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public Zone() {
      // for reflective instantiation.
    }

    public Zone(String name) {
      this(name, null);
    }

    public Zone(String name, String id) {
      put("name", name);
      if (id != null) {
        put("id", id);
      }
    }
  }

  /** Enabled via {@link feign.Feign.Builder#decode404()} */
  @Test
  public void notFoundDecodesToEmpty() throws Exception {
    Response response = Response.builder()
            .status(404)
            .reason("NOT FOUND")
            .headers(Collections.<String, Collection<String>>emptyMap())
            .build();
    assertThat((byte[]) new FastjsonDecoder().decode(response, byte[].class)).isEmpty();
  }
}
