package main;

//Contains some extra useful data
public class MetaSamples extends Samples {
	private static final long serialVersionUID = 1L;
	
	public double speedMult;
	public double startPos;
	public double endPos;
	public double length;
	
	public MetaSamples(int samplerate, double[] sampledata) {
		super(samplerate, sampledata);
	}
	public MetaSamples(Samples original){
		super(original.sampleRate,original.sampleData);
		spectrumReal = original.spectrumReal;
		spectrumImag = original.spectrumImag;
	}

	public static MetaSamples blankSamplesFrom(MetaSamples original){
		MetaSamples result = new MetaSamples(original);
		result.speedMult=original.speedMult;
		result.startPos=original.startPos;
		result.endPos=original.endPos;
		result.length=original.length;
		return result;
	}
}
