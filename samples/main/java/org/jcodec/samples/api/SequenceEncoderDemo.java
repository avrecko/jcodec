package org.jcodec.samples.api;

import org.jcodec.api.awt.AWTSequenceEncoder8Bit;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.common.tools.MainUtils;
import org.jcodec.common.tools.MainUtils.Cmd;
import org.jcodec.common.tools.MainUtils.Flag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author Stanislav Vitvitskyy
 * 
 */
public class SequenceEncoderDemo {
    private static final Flag FLAG_FPS = new Flag("fps", "fps", "Rational fps, i.e 25:1 (for 25fps), 30:1 (for 30fps), 30000:1001 (for 29.97fps), etc.");
    private static final Flag FLAG_FRAMES = new Flag("num-frames", "num-frames", "Maximum frames to decode.");
    private static final Flag FLAG_PATTERN = new Flag("out-pattern", "out-pattern", "Input folder/frame%04.png pattern.");
    private static final Flag[] FLAGS = new MainUtils.Flag[] {FLAG_FPS, FLAG_FRAMES, FLAG_PATTERN};

    public static void main(String[] args) throws IOException {
        Cmd cmd = MainUtils.parseArguments(args, FLAGS);
        if (cmd.argsLength() < 1) {
            MainUtils.printHelpVarArgs(FLAGS, "output file name");
            return;
        }

        int maxFrames = cmd.getIntegerFlagD(FLAG_FRAMES, Integer.MAX_VALUE);
        String fpsRaw = cmd.getStringFlagD(FLAG_FPS, "25:1");
        String outDir = cmd.getStringFlagD(FLAG_PATTERN,
                new File(System.getProperty("user.home"), "frame%08d.jpg").getAbsolutePath());
        FileChannelWrapper out = null;
        try {
            out = NIOUtils.writableChannel(MainUtils.tildeExpand(cmd.getArg(0)));
            AWTSequenceEncoder8Bit encoder = new AWTSequenceEncoder8Bit(out, Rational.parse(fpsRaw));

            for (int i = 0; i < maxFrames; i++) {
                File file = new File(String.format(outDir, i));
                if (!file.exists())
                    break;
                BufferedImage image = ImageIO.read(file);
                encoder.encodeImage(image);
            }
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }
}
