package org.projectempire.lx.vstrip;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXDynamicColor;
import heronarts.lx.color.LXSwatch;

import java.util.Random;
import java.util.TreeMap;

public final class Colors {
  static final Random rand = new Random();

  private Colors() {
  }

  /**
   * Returns the red part of a 32-bit RGBA color.
   */
  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  /**
   * Returns the green part of a 32-bit RGBA color.
   */
  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  /**
   * Returns the blue part of a 32-bit RGBA color.
   */
  public static int blue(int color) {
    return color & 0xff;
  }

  /**
   * Returns the alpha part of a 32-bit RGBA color.
   */
  public static int alpha(int color) {
    return (color >> 24) & 0xff;
  }

  /**
   * Returns a color constructed from the three components. The alpha component is set to 255.
   */
  public static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  public static int rgba(int r, int g, int b, int a) {
    return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  /**
   * Given a range from 0 to one, extract a color out of a swatch.  0 is the left most color of
   * the palette and 1 is the rightmost.  A swatch returns a DynamicColor, which can already lerp
   * between the base color and the next color.  So here, we just need to remap 0 to 1 so that
   * it pulls the correct DynamicColor from the correct index of the Swatch and then re-parameterize
   * our 0 to 1 value.  For example with 4 colors, 0.125 is halfway between the first two colors.
   * With 4 colors, we have 0.250 per color. So colorIndexRange = 1.0 / NumSwatchColors;
   * (int)(0.125 / colorIndexRange) -> 0, so we use index 0 on the swatch.  Now to reparameterize
   * we just divide 0.125/0.250 = 0.5;  So we can just LXColor.lerp between DynamicColor.primary and DynamicColor.secondary;
   */
  static public int getParameterizedPaletteColor(LX lx, int swatchIndex, float t, EaseUtil ease) {
    if (swatchIndex >= lx.engine.palette.swatches.size())
      return 0;
    LXSwatch swatch = lx.engine.palette.swatches.get(swatchIndex);
    if (swatch.colors.isEmpty())
      return LXColor.BLACK;
    if (swatch.colors.size() == 1)
      return swatch.getColor(0).primary.getColor();
    float colorIndexRange = 1.0f / (float)((swatch.colors.size()-1));
    int colorIndex = (int) (t / colorIndexRange);
    if (t < 0f)
      t = 0f;

    int nextIndex = colorIndex + 1;
    if (nextIndex >= swatch.colors.size())
      nextIndex -= 1;
    float distanceInRange = (t - colorIndex * colorIndexRange)/colorIndexRange;
    if (ease!= null) distanceInRange = ease.ease(distanceInRange);
    LXDynamicColor color = swatch.getColor(colorIndex);
    LXDynamicColor nextColor = swatch.getColor(nextIndex);
    return LXColor.lerp(color.primary.getColor(), nextColor.primary.getColor(), distanceInRange);
  }

  public static class KelvinToRGB {
    private static final TreeMap<Integer, int[]> kelvinTable = new TreeMap<>();

    static {
      // Populate the kelvin table
      kelvinTable.put(1000, new int[]{255, 56, 0});
      kelvinTable.put(1100, new int[]{255, 71, 0});
      kelvinTable.put(1200, new int[]{255, 83, 0});
      kelvinTable.put(1300, new int[]{255, 93, 0});
      kelvinTable.put(1400, new int[]{255, 101, 0});
      kelvinTable.put(1500, new int[]{255, 109, 0});
      kelvinTable.put(1600, new int[]{255, 115, 0});
      kelvinTable.put(1700, new int[]{255, 121, 0});
      kelvinTable.put(1800, new int[]{255, 126, 0});
      kelvinTable.put(1900, new int[]{255, 131, 0});
      kelvinTable.put(2000, new int[]{255, 138, 18});
      kelvinTable.put(2100, new int[]{255, 142, 33});
      kelvinTable.put(2200, new int[]{255, 147, 44});
      kelvinTable.put(2300, new int[]{255, 152, 54});
      kelvinTable.put(2400, new int[]{255, 157, 63});
      kelvinTable.put(2500, new int[]{255, 161, 72});
      kelvinTable.put(2600, new int[]{255, 165, 79});
      kelvinTable.put(2700, new int[]{255, 169, 87});
      kelvinTable.put(2800, new int[]{255, 173, 94});
      kelvinTable.put(2900, new int[]{255, 177, 101});
      kelvinTable.put(3000, new int[]{255, 180, 107});
      kelvinTable.put(3100, new int[]{255, 184, 114});
      kelvinTable.put(3200, new int[]{255, 187, 120});
      kelvinTable.put(3300, new int[]{255, 190, 126});
      kelvinTable.put(3400, new int[]{255, 193, 132});
      kelvinTable.put(3500, new int[]{255, 196, 137});
      kelvinTable.put(3600, new int[]{255, 199, 143});
      kelvinTable.put(3700, new int[]{255, 201, 148});
      kelvinTable.put(3800, new int[]{255, 204, 153});
      kelvinTable.put(3900, new int[]{255, 206, 159});
      kelvinTable.put(4000, new int[]{255, 209, 163});
      kelvinTable.put(4100, new int[]{255, 211, 168});
      kelvinTable.put(4200, new int[]{255, 213, 173});
      kelvinTable.put(4300, new int[]{255, 215, 177});
      kelvinTable.put(4400, new int[]{255, 217, 182});
      kelvinTable.put(4500, new int[]{255, 219, 186});
      kelvinTable.put(4600, new int[]{255, 221, 190});
      kelvinTable.put(4700, new int[]{255, 223, 194});
      kelvinTable.put(4800, new int[]{255, 225, 198});
      kelvinTable.put(4900, new int[]{255, 227, 202});
      kelvinTable.put(5000, new int[]{255, 228, 206});
      kelvinTable.put(5100, new int[]{255, 230, 210});
      kelvinTable.put(5200, new int[]{255, 232, 213});
      kelvinTable.put(5300, new int[]{255, 233, 217});
      kelvinTable.put(5400, new int[]{255, 235, 220});
      kelvinTable.put(5500, new int[]{255, 236, 224});
      kelvinTable.put(5600, new int[]{255, 238, 227});
      kelvinTable.put(5700, new int[]{255, 239, 230});
      kelvinTable.put(5800, new int[]{255, 240, 233});
      kelvinTable.put(5900, new int[]{255, 242, 236});
      kelvinTable.put(6000, new int[]{255, 243, 239});
      kelvinTable.put(6100, new int[]{255, 244, 242});
      kelvinTable.put(6200, new int[]{255, 245, 245});
      kelvinTable.put(6300, new int[]{255, 246, 247});
      kelvinTable.put(6400, new int[]{255, 248, 251});
      kelvinTable.put(6500, new int[]{255, 249, 253});
      kelvinTable.put(6600, new int[]{254, 249, 255});
      kelvinTable.put(6700, new int[]{252, 247, 255});
      kelvinTable.put(6800, new int[]{249, 246, 255});
      kelvinTable.put(6900, new int[]{247, 245, 255});
      kelvinTable.put(7000, new int[]{245, 243, 255});
      kelvinTable.put(7100, new int[]{243, 242, 255});
      kelvinTable.put(7200, new int[]{240, 241, 255});
      kelvinTable.put(7300, new int[]{239, 240, 255});
      kelvinTable.put(7400, new int[]{237, 239, 255});
      kelvinTable.put(7500, new int[]{235, 238, 255});
      kelvinTable.put(7600, new int[]{233, 237, 255});
      kelvinTable.put(7700, new int[]{231, 236, 255});
      kelvinTable.put(7800, new int[]{230, 235, 255});
      kelvinTable.put(7900, new int[]{228, 234, 255});
      kelvinTable.put(8000, new int[]{227, 233, 255});
      kelvinTable.put(8100, new int[]{225, 232, 255});
      kelvinTable.put(8200, new int[]{224, 231, 255});
      kelvinTable.put(8300, new int[]{222, 230, 255});
      kelvinTable.put(8400, new int[]{221, 230, 255});
      kelvinTable.put(8500, new int[]{220, 229, 255});
      kelvinTable.put(8600, new int[]{218, 229, 255});
      kelvinTable.put(8700, new int[]{217, 227, 255});
      kelvinTable.put(8800, new int[]{216, 227, 255});
      kelvinTable.put(8900, new int[]{215, 226, 255});
      kelvinTable.put(9000, new int[]{214, 225, 255});
      kelvinTable.put(9100, new int[]{212, 225, 255});
      kelvinTable.put(9200, new int[]{211, 224, 255});
      kelvinTable.put(9300, new int[]{210, 223, 255});
      kelvinTable.put(9400, new int[]{209, 223, 255});
      kelvinTable.put(9500, new int[]{208, 222, 255});
      kelvinTable.put(9600, new int[]{207, 221, 255});
      kelvinTable.put(9700, new int[]{207, 221, 255});
      kelvinTable.put(9800, new int[]{206, 220, 255});
      kelvinTable.put(9900, new int[]{205, 220, 255});
      kelvinTable.put(10000, new int[]{207, 218, 255});
      kelvinTable.put(10100, new int[]{207, 218, 255});
      kelvinTable.put(10200, new int[]{206, 217, 255});
      kelvinTable.put(10300, new int[]{205, 217, 255});
      kelvinTable.put(10400, new int[]{204, 216, 255});
      kelvinTable.put(10500, new int[]{204, 216, 255});
      kelvinTable.put(10600, new int[]{203, 215, 255});
      kelvinTable.put(10700, new int[]{202, 215, 255});
      kelvinTable.put(10800, new int[]{202, 214, 255});
      kelvinTable.put(10900, new int[]{201, 214, 255});
      kelvinTable.put(11000, new int[]{200, 213, 255});
      kelvinTable.put(11100, new int[]{200, 213, 255});
      kelvinTable.put(11200, new int[]{199, 212, 255});
      kelvinTable.put(11300, new int[]{198, 212, 255});
      kelvinTable.put(11400, new int[]{198, 212, 255});
      kelvinTable.put(11500, new int[]{197, 211, 255});
      kelvinTable.put(11600, new int[]{197, 211, 255});
      kelvinTable.put(11700, new int[]{197, 210, 255});
      kelvinTable.put(11800, new int[]{196, 210, 255});
      kelvinTable.put(11900, new int[]{195, 210, 255});
      kelvinTable.put(12000, new int[]{195, 209, 255});
    }

    public static int[] kelvinToRGBArray(double kelvin) {
      // Round the input kelvin to the nearest 100
      int roundedKelvin = (int) (Math.round(kelvin / 100.0) * 100);

      // Clamp the value between 1000K and 12000K
      roundedKelvin = Math.max(1000, Math.min(12000, roundedKelvin));

      // Get the exact match or the closest lower temperature
      Integer floorKey = kelvinTable.floorKey(roundedKelvin);

      // If there's no lower key, use the lowest available temperature
      if (floorKey == null) {
        return kelvinTable.firstEntry().getValue();
      }

      return kelvinTable.get(floorKey);
    }

    public static int kelvinToRGB(double kelvin) {
      int[] rgb = kelvinToRGBArray(kelvin);
      return LXColor.rgb(rgb[0], rgb[1], rgb[2]);
    }
  }
}
