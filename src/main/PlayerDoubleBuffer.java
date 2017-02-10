package main;

import java.nio.*;
import javax.sound.sampled.*;

//Uses a double buffer to allow real time sound
public class PlayerDoubleBuffer implements Player {
	
	/*
	 * Don't modify this, ever
	 * Empty time bounds
	 */
	private static final double[] emptyBounds = new double[]{Double.MAX_VALUE,Double.MIN_VALUE};
	
	//Continue looping
	private volatile boolean cont = true;
	public volatile double volume = 32768d;
	public int bufferSize;
	
	//The other thread needed
	private class TrackPoller extends Thread{
		//The track to poll
		public Track toPoll;
		public MetaSamples result;
		public boolean done;
		
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
	
	public PlayerDoubleBuffer(int bufferSize){
		this.bufferSize = bufferSize;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroySelf() {
		// TODO Auto-generated method stub

	}

	@Override
	public void playTrack(Track track, boolean loop) {
		cont = true;
		
		//Sanity checks
		double[] timeBounds = track.getTimeBounds();
		if(timeBounds!=null&&timeBounds!=emptyBounds){
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
						//TODO speed mult
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
	public void stopPlay() {
		cont=false;
	}

}
