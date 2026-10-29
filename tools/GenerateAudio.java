import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/** Original mathematical sound design, with no samples or third-party recordings. */
public final class GenerateAudio {
    private static final int RATE = 22050;
    private static final double TAU = Math.PI * 2;

    public static void main(String[] args) throws IOException {
        Path folder = args.length == 0 ? Path.of("assets/audio") : Path.of(args[0]);
        Files.createDirectories(folder);
        write(folder.resolve("ambient.wav"), 6, "ambient");
        write(folder.resolve("combat.wav"), 4, "combat");
        write(folder.resolve("click.wav"), .085, "click");
        write(folder.resolve("impact.wav"), .18, "impact");
        write(folder.resolve("weakness.wav"), .42, "weakness");
    }

    private static void write(Path path, double duration, String kind) throws IOException {
        int samples = (int)(duration * RATE);
        ByteBuffer wave = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN);
        wave.put(new byte[]{'R','I','F','F'}).putInt(36+samples*2).put(new byte[]{'W','A','V','E'});
        wave.put(new byte[]{'f','m','t',' '}).putInt(16).putShort((short)1).putShort((short)1);
        wave.putInt(RATE).putInt(RATE*2).putShort((short)2).putShort((short)16);
        wave.put(new byte[]{'d','a','t','a'}).putInt(samples*2);
        Random random = new Random(2041);
        double filtered = 0;
        for (int i=0;i<samples;i++) {
            double t = i/(double)RATE, n = random.nextDouble()*2-1;
            filtered = filtered*.94+n*.06;
            double value;
            switch(kind) {
                case "ambient" -> {
                    // Every frequency and modulation completes an integer number of cycles in 6 seconds.
                    double breathe = .76+.18*Math.cos(TAU*t/6);
                    value = breathe*(.22*Math.sin(TAU*55*t)+.11*Math.sin(TAU*82.5*t)+.06*Math.sin(TAU*110*t));
                    value += .018*Math.sin(TAU*440*t)*Math.pow(.5+.5*Math.cos(TAU*t/3),4);
                }
                case "combat" -> {
                    double beat = t*2-Math.floor(t*2);
                    double kick = Math.exp(-beat*20)*Math.sin(TAU*(55*t+2.5*(1-Math.exp(-beat*15))));
                    double pulse = .6+.4*Math.cos(TAU*t/4);
                    value = .26*kick+.13*Math.sin(TAU*55*t)*pulse+.07*Math.sin(TAU*73.5*t);
                    value += filtered*.12*Math.exp(-Math.pow((beat-.25)*35,2));
                    value *= Math.min(1,Math.min(t/.012,(duration-t)/.012));
                }
                case "click" -> value = .3*Math.sin(TAU*880*t)*Math.exp(-t*52)*Math.min(1,t/.004);
                case "impact" -> value = (.55*filtered+.3*Math.sin(TAU*(76*t-90*t*t)))*Math.exp(-t*23)*Math.min(1,t/.002);
                case "weakness" -> {
                    double attack = Math.min(1,t/.012), release = Math.pow(Math.max(0,1-t/duration),1.8);
                    value = attack*release*(.2*Math.sin(TAU*440*t)+.16*Math.sin(TAU*660*t)+.1*Math.sin(TAU*880*t));
                }
                default -> throw new IllegalArgumentException(kind);
            }
            wave.putShort((short)Math.round(Math.max(-.95,Math.min(.95,value))*32767));
        }
        Files.write(path,wave.array());
        System.out.println(path+" ("+samples+" amostras)");
    }
}
