package Channel;

public class Channel_Triangle extends APUChannel
{
    // Fields
    long dat;
    private double _frequency;
    private static byte[] _lengthValues = new byte[] {0x5 * 2, (byte) (0x7f * 2), 0xA * 2, 0x1 * 2, 0x14 * 2, 0x2 * 2,
            0x28 * 2, 0x3 * 2, (byte) (0x50 * 2), 0x4 * 2, 0x1E * 2, 0x5 * 2, 0x7 * 2, 0x6 * 2, 0x0E * 2, 0x7 * 2,
            0x6 * 2, 0x08 * 2, 0xC * 2, 0x9 * 2, 0x18 * 2, 0xa * 2, 0x30 * 2, 0xb * 2, (byte) (0x60 * 2), 0xc * 2,
            0x24 * 2, 0xd * 2, 0x8 * 2, 0xe * 2, 0x10 * 2, 0xf * 2 };
    private int _linearCounter;
    private int _linearCounterLoad;
    private double _renderedWavelength;
    private int _rightShift;
    private long _sequence;
    public static byte[] _sequenceData = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 14, 13,
            12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };
    private boolean _triangleHalt;

    // Methods
    public Channel_Triangle(double samplingRate) {
        super(samplingRate);
    }

    public int RenderSample() {
//        if (((super.getLengthCounter() > 0) && (this.getLinearCounter() > 0)) && (super.getWavelength() > 0)) {
//            super.setSampleCount(super.getSampleCount() + 1);
//            if (super.getSampleCount() >= this._renderedWavelength) {
//                super.setSampleCount(super.getSampleCount() - this._renderedWavelength);
//                this._sequence++;
//            }
//            int x =  (int) (_sequenceData[(int) (this.getSequence() & 0x1f)] * 0x40);
//            if (x >256)
//                System.out.println();
////                x = 0;
//            return x;
//        }
        return 0;
//         return (long)(_sequenceData[(int) (this.getSequence() & 0x1f)] * 0x40);
    }

    public void UpdateEnvelope() {
    }

    private void UpdateFrequency() {
        this._frequency = super.getClockSpeed() / (super.getWavelength() + 1);
        this._renderedWavelength = super._samplingRate / this._frequency;
    }

    public void UpdateLinearCounter() {
        if (!super.getLengthCounterDisable()) {
            this.setLinearCounter(this.getLinearCounter() - 1);
            if (this.getLinearCounter() < 0) {
                this.setLinearCounter(0);
            }
        }
    }

    public void UpdateSweep() {
    }

    public void WriteReg1(byte b) {
        super.setLengthCounterDisable((b & 0x80) != 0);
        this.setLinearCounterLoad((b & 0x7f)&0xff);
    }

    public void WriteReg2(byte b) {
        this.setRightShift((b & 3)&0xff);
    }

    public void WriteReg3(byte b) {
        super.setWavelength(((super.getWavelength() & 0x700) | b)&0xff);
        this.UpdateFrequency();
    }

    public void WriteReg4(byte b) {
        super.setWavelength((super.getWavelength() & 0xff) | ((b & 7) << 8));
        this.UpdateFrequency();
        super.setLengthCounter((_lengthValues[(b & 0xf8) >> 3]) & 0xff);
        this.setTriangleHalt(true);
        this.setLinearCounter(this.getLinearCounterLoad());
    }

    public int getLinearCounter() {
        return this._linearCounter;
    }

    public void setLinearCounter(int value) {
        if (value < -1)
            System.out.print("");
        this._linearCounter = value;
    }

    public int getLinearCounterLoad() {
        return this._linearCounterLoad;
    }

    public void setLinearCounterLoad(int value) {
        if (value < -1)
            System.out.print("");
        this._linearCounterLoad = value;
    }

    public int getRightShift() {
        return this._rightShift;
    }

    public void setRightShift(int value) {
        if (value < -1)
            System.out.print("");
        this._rightShift = value;
    }

    public long getSequence() {
        return this._sequence;
    }

    public void setSequence(long value) {
        if (value < -1)
            System.out.print("");
        this._sequence = value;
    }

    public boolean getTriangleHalt() {
        return this._triangleHalt;
    }

    public void setTriangleHalt(boolean value) {
        this._triangleHalt = value;
    }
}
