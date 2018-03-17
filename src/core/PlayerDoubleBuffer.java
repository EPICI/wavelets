package core;

import java.nio.*;
import javax.sound.sampled.*;

/**
 * An implementation of the {@link Player} interface which
 * uses a double buffer to allow for continuous audio playback
 * 
 * @author EPICI
 * @version 1.0
 */
public class PlayerDoubleBuffer implements Player {
	
	/**
	 * Flag that says if it should continue looping
	 * <br>
	 * Set to false to stop playing
	 */
	private volatile boolean cont = true;
	/**
	 * Volume multiplier, default is fine since values will be shorts
	 * and the amplitude is expected to cap at 1.0
	 */
	public volatile double volume = 32768d;
	/**
	 * Buffer size
	 */
	public int bufferSize;
	/**
	 * The current time, if sound is playing
	 */
	public volatile double currentTime;
	
	/**
	 * Destroyed yet?
	 */
	protected transient boolean destroyed = false;
	
	/**
	 * The other thread which works in conjunction with this one
	 * to provide audio
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	private class TrackPoller extends Thread{
		/**
		 * The track to poll
		 */
		public Track toPoll;
		/**
		 * Results stored here
		 */
		public MetaSamples result;
		/**
		 * True when done, should always finish in time
		 */
		public boolean done;
		
		/**
		 * Standard constructor
		 * 
		 * @param toPoll track to poll
		 * @param copyFrom {@link MetaSamples} to copy from
		 */
		public TrackPoller(Track toPoll,MetaSamples copyFrom){
			this.toPoll = toPoll;
			result = MetaSamples.blankSamplesFrom(copyFrom);
			done = false;
		}
		
		public void run(){
			done = false;
			toPoll.applyTo(result);
			done = true;
			notifyAll();
		}
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param bufferSize buffer size
	 */
	public PlayerDoubleBuffer(int bufferSize){
		this.bufferSize = bufferSize;
	}

	@Override
	public void destroy() {
		stopPlay();
		destroyed = true;
	}

	@Override
	public void destroySelf() {
		stopPlay();
		destroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void playTrack(Track track, boolean loop) {
		cont = true;
		
		//Sanity checks
		double[] timeBounds = track.getTimeBounds();
		if(timeBounds!=null&&timeBounds[0]<Double.MAX_VALUE&&timeBounds[1]>Double.MIN_VALUE){
			if(timeBounds[1]>timeBounds[0]){
				//Format object
				AudioFormat audioFormat;
				//Output
				SourceDataLine sourceDataLine;
				//Allowable 8000,11025,16000,22050,44100
				float sampleRate = 44100f;
				//Allowable 8,16
				int sampleSizeInBits = 16;
				//Allowable 1,2
				int channels = 1;
				//Allowable true,false
				boolean signed = true;
				//Allowable true,false
				boolean bigEndian = true;
				
				byte[] audioData = new byte[bufferSize*2];
				
				ByteBuffer byteBuffer;
				ShortBuffer shortBuffer;
				
				byteBuffer = ByteBuffer.wrap(audioData);
				shortBuffer = byteBuffer.asShortBuffer();
				
				audioFormat = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
				DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat);
				//Get a SourceDataLine object
				try{
					sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

					try{
						//Open and start the SourceDataLine
						sourceDataLine.open(audioFormat);
						sourceDataLine.start();

						double[][] doubleBuffer = new double[2][];
						double secondLength = bufferSize/sampleRate;
						long timeout = (long)(secondLength*1000d)+1;
						MetaSamples copySamples = MetaSamples.blankSamples(44100,bufferSize);
						doubleBuffer[1] = copySamples.sampleData;
						copySamples.composition = track.parentComposition();
						copySamples.length = secondLength;
						copySamples.endPos = timeBounds[0];
						copySamples.pushToNext();
						TrackPoller tp = new TrackPoller(track,copySamples);
						tp.run();//Intentional
						while(cont){
							copySamples.endPos=timeBounds[0];
							while(copySamples.endPos<timeBounds[1]){
								if(!tp.done){
									tp.wait(timeout);
								}
								tp = new TrackPoller(track,copySamples);
								double[] audioDataDouble = doubleBuffer[0] = doubleBuffer[1];
								for(int i=0;i<bufferSize;i++){
									shortBuffer.put(i, (short)(audioDataDouble[i]*volume));
								}
								copySamples.pushToNext();
								doubleBuffer[1]=tp.result.sampleData;
								tp.start();
								sourceDataLine.write(audioData, 0, bufferSize*2);
								currentTime = copySamples.startPos;// Keep it updated
							}
							if(!loop){
								break;
							}
						}
						
						//Block and wait for internal buffer of the
						// SourceDataLine to become empty.
						sourceDataLine.drain();
						
						//Finish with the SourceDataLine
						sourceDataLine.stop();
						sourceDataLine.close();
						
					}catch (Exception e) {
						e.printStackTrace();
					}
				}catch(LineUnavailableException e){
					
				}
			}
		}
	}

	@Override
	public void playSamples(Samples samples, boolean loop) {
		cont = true;
		//Format object
		AudioFormat audioFormat;
		//Output
		SourceDataLine sourceDataLine;
		//Allowable 8000,11025,16000,22050,44100
		float sampleRate = 44100f;
		//Allowable 8,16
		int sampleSizeInBits = 16;
		//Allowable 1,2
		int channels = 1;
		//Allowable true,false
		boolean signed = true;
		//Allowable true,false
		boolean bigEndian = true;
		
		double[] audioDataDouble = samples.sampleData;
		int sampleCount = audioDataDouble.length;
		byte[] audioData = new byte[sampleCount*2];
		
		ByteBuffer byteBuffer;
		ShortBuffer shortBuffer;
		
		byteBuffer = ByteBuffer.wrap(audioData);
		shortBuffer = byteBuffer.asShortBuffer();
		
		for(double v:audioDataDouble){
			shortBuffer.put((short) (v*volume));
		}
		
		audioFormat = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat);
		//Get a SourceDataLine object
		try{
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			try{
				//Open and start the SourceDataLine
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();

				while(cont){
					// Due to sending all the data at once, we can't know the time
					sourceDataLine.write(audioData, 0, sampleCount*2);
					if(!loop){
						break;
					}
				}
				
				//Block and wait for internal buffer of the
				// SourceDataLine to become empty.
				sourceDataLine.drain();
				
				//Finish with the SourceDataLine
				sourceDataLine.stop();
				sourceDataLine.close();
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}catch(LineUnavailableException e){
			
		}
	}

	@Override
	public double currentTime() {
		return currentTime;
	}
	
	@Override
	public boolean isPlaying() {
		return cont;
	}
	
	@Override
	public void stopPlay() {
		cont=false;
	}

}
