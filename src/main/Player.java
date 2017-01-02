package main;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player extends Thread {
	//Currently playing
	volatile boolean playing = false;
	volatile boolean cont = true;
	volatile boolean queued = false;
	//Format object
	AudioFormat audioFormat;
	//Input stream object
	AudioInputStream audioInputStream;
	//Output
	SourceDataLine sourceDataLine;
	float sampleRate = 44100f;
	//Allowable 8000,11025,16000,22050,44100
	int sampleSizeInBits = 16;
	//Allowable 8,16
	int channels = 1;
	//Allowable 1,2
	boolean signed = true;
	//Allowable true,false
	boolean bigEndian = true;
	//Allowable true,false
	volatile short[] audioDataShort;
	//Data in short form
	
	public void run(){
		while(true){
			if(queued){
				playing = true;
				cont = true;
				
				byte[] audioData = new byte[audioDataShort.length*2];
				
				ByteBuffer byteBuffer;
				ShortBuffer shortBuffer;
				
				byteBuffer = ByteBuffer.wrap(audioData);
				shortBuffer = byteBuffer.asShortBuffer();
				
				shortBuffer.put(audioDataShort);
				
				audioFormat = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
				InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
				audioInputStream = new AudioInputStream(byteArrayInputStream,audioFormat,audioData.length/audioFormat.getFrameSize());
				DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat);
				
				//Get a SourceDataLine object
				try{
					sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
				}catch(LineUnavailableException e){
					
				}
				
				//This is a working buffer used to transfer
				//the data between the AudioInputStream and
				//the SourceDataLine. The size is rather arbitrary.
				byte playBuffer[] = new byte[16384];
				try{
					//Open and start the SourceDataLine
					sourceDataLine.open(audioFormat);
					sourceDataLine.start();

					int cnt;

					//Transfer the audio data to the speakers
					while(((cnt = audioInputStream.read(playBuffer, 0,playBuffer.length))!= -1)&&cont){
						//Keep looping until the input read
						// method returns -1 for empty stream.
						if(cnt > 0){
							//Write data to the internal buffer of
							//the data line where it will be
							//delivered to the speakers in real time
							sourceDataLine.write(playBuffer, 0, cnt);
						}//end if
					}//end while
					
					//Block and wait for internal buffer of the
					// SourceDataLine to become empty.
					sourceDataLine.drain();
					
					//Finish with the SourceDataLine
					sourceDataLine.stop();
					
					sourceDataLine.close();
					
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				playing = false;
				queued = false;
			}else{
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void stopSound(){
		cont = false;
	}
	
	public void playSound(short[] audioData){
		if(playing){
			stopSound();
			while(playing){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
		
		audioDataShort = audioData;
		queued = true;
	}
}
