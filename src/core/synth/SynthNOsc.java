package core.synth;

import java.awt.Color;
import java.util.*;
import javax.swing.JInternalFrame;
import org.python.core.PyObject;
import core.*;
import util.*;
import util.waveform.*;
import util.jython.*;
import util.math.*;
import util.ds.*;

/**
 * The Nx Osc synth. A direct upgrade from 3x Osc.
 * 
 * @author EPICI
 * @version 1.0
 */
public class SynthNOsc implements Synthesizer {
	
	private static final double SEMITONE = StrictMath.pow(2d, 1d/12d);
	
	/**
	 * Simple oscillators
	 */
	public final List<Osc> oscillators;
	
	/**
	 * The parent composition
	 */
	protected transient Composition parentComposition;
	
	/**
	 * Destroyed yet?
	 */
	protected transient boolean destroyed = false;
	/**
	 * The name of this synthesizer. Use getter and setter instead of direct access.
	 */
	public String name;
	
	/**
	 * Default constructor
	 */
	public SynthNOsc(Composition parent){
		oscillators = Collections.synchronizedList(new ArrayList<Osc>());
		initTransient(parent);
	}

	@Override
	public void initTransient(Composition parent) {
		parentComposition = parent;
	}

	@Override
	public void destroy() {
		oscillators.clear();
		destroyed = true;
	}

	@Override
	public void destroySelf() {
		oscillators.clear();
		destroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void spawnVoices(double[][] clips, TrackLayerSimple target, Session session) {
		int n = oscillators.size();
		Osc[] losc = oscillators.toArray(new Osc[n]);
		for(double[] clip:clips){
			Voice[] oscvoices = new Voice[n];
			for(int i=0;i<n;i++){
				Osc.OscVoice added = losc[i].spawn(clip[2], clip[0], clip[1], clip[3]);
				added.delay = clip[0];
				oscvoices[i]=added;
			}
			target.addVoice(Voice.combine(oscvoices));
		}
	}

	@Override
	public Voice spawnLiveVoice(int[] params, Session session) {
		double ctime = session.getCurrentTime();
		int pitch = params[0];
		int n = oscillators.size();
		Voice[] all = new Voice[n];
		for(int i=0;i<n;i++){
			Osc.OscVoice added = oscillators.get(i).spawn(pitch, ctime, ctime+Floats.ID_TINY);
			all[i] = added;
			added.step = 1;
		}
		return Voice.combine(all);
	}

	@Override
	public MetaComponent<? extends JInternalFrame> getUI() {
		return null;//TODO
	}

	@Override
	public void setGlobals(HashMap<String, Object> vars) {
		
	}

	@Override
	public boolean isPython() {
		return false;
	}

	@Override
	public Factory<Voice> getVoiceFactory() {
		return null;//TODO
	}

	@Override
	public PyObject getPvfInfo() {
		return null;
	}
	
	@Override
	public Color getColorSignature(double time){
		long mix = -1;
		for(Osc osc:oscillators){
			mix += osc.type*17+sig(osc.getDetune(time))
			+(sig(osc.getVolume(time))^sig(osc.getMinVolume(time)))
			+(sig(osc.getAttackConst(time))^sig(osc.getAttackFrac(time)))
			+(sig(osc.getHoldConst(time))^sig(osc.getHoldFrac(time)))
			+(sig(osc.getDecayConst(time))^sig(osc.getDecayFrac(time)));
		}
		return Color.getHSBColor(
				0x1.0p-32f*(mix&0xffffffffL),
				0.4f+(0.6f*0x1.0p-16f)*((mix>>32)&0xffffL),
				0.6f+(0.4f*0x1.0p-16f)*(mix>>>48)
				);
	}
	
	/**
	 * Characteristic signature
	 * 
	 * @param v a value
	 * @return a compressed view
	 */
	private static long sig(double v){
		long bits = Double.doubleToLongBits(v);
		return bits>>>48;
	}
	
	@Override
	public String getName(){
		if(name==null || name.length()==0)return Track.defaultNameAny("Synthesizer", this);
		return name;
	}
	
	@Override
	public boolean setName(String newName){
		name = newName;
		return true;
	}
	
	// TODO BetterClone implementation
	
	// TODO makeDefaultSynth(Session) static method, use single saw osc
	
	private static final String OSC_CLASS_NAME = Osc.class.getCanonicalName();
	private static final String[] OSC_PROPERTIES_NAMES = {
			"detune",
			"volume",
			"attackConst",
			"attackFrac",
			"holdConst",
			"holdFrac",
			"decayConst",
			"decayFrac",
			"minVolume",
	};
	
	/**
	 * An oscillator used by this class
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public class Osc implements BetterClone<Osc>{
		/**
		 * Primitive waveform used
		 * <table><thead>
		 * <tr><th>Type</th><th>ID</th></tr>
		 * </thead><tbody>
		 * <tr><td>Sine</td><td>0</td></tr>
		 * <tr><td>Square</td><td>1</td></tr>
		 * <tr><td>Triangle</td><td>2</td></tr>
		 * <tr><td>Saw</td><td>3</td></tr>
		 * </tbody></table>
		 */
		public volatile int type;
		/**
		 * Holds the other properties
		 * <br>
		 * Arrays allow for easy automation
		 */
		public final VarDouble[] properties;
		
		public Osc(){
			type = 0;
			properties = VarDouble.wrapCopy(new double[]{
					0d,//detune
					0d,//volume
					0d,//attackConst
					0d,//attackFrac
					0d,//holdConst
					1d,//holdFrac
					0d,//decayConst
					1d,//decayFrac
					0d,//minVolume
					});
		}
		/**
		 * Detune in semitones
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getDetune(double time) {
			return properties[0].get(time);
		}

		/**
		 * Detune in semitones
		 * 
		 * @return
		 */
		public synchronized void setDetune(double detune) {
			properties[0].set(detune);;
		}

		/**
		 * Volume offset in B
		 * <br>
		 * Determines peak for envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getVolume(double time) {
			return properties[1].get(time);
		}

		/**
		 * Volume offset in B
		 * <br>
		 * Determines peak for envelope
		 * 
		 * @param volume
		 */
		public synchronized void setVolume(double volume) {
			properties[1].set(volume);;
		}

		/**
		 * Duration in measures to reach full volume
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getAttackConst(double time) {
			return properties[2].get(time);
		}

		/**
		 * Duration in measures to reach full volume
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param attackConst
		 */
		public synchronized void setAttackConst(double attackConst) {
			final double t=Floats.D_TINY;
			properties[2].set(attackConst<t?t:attackConst);
		}

		/**
		 * Fraction of note length to reach full volume
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getAttackFrac(double time) {
			return properties[3].get(time);
		}

		/**
		 * Fraction of note length to reach full volume
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param attackFrac
		 */
		public synchronized void setAttackFrac(double attackFrac) {
			final double t=Floats.D_TINY;
			properties[3].set(attackFrac<t?t:attackFrac);
		}

		/**
		 * Duration in measures to hold at full volume
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getHoldConst(double time) {
			return properties[4].get(time);
		}

		/**
		 * Duration in measures to hold at full volume
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param holdConst
		 */
		public synchronized void setHoldConst(double holdConst) {
			final double t=Floats.D_TINY;
			properties[4].set(holdConst<t?t:holdConst);
		}

		/**
		 * Fraction of note length to hold at full volume
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getHoldFrac(double time) {
			return properties[5].get(time);
		}

		/**
		 * Fraction of note length to hold at full volume
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param holdFrac
		 */
		public synchronized void setHoldFrac(double holdFrac) {
			final double t=Floats.D_TINY;
			properties[5].set(holdFrac<t?t:holdFrac);
		}

		/**
		 * Volume reduction per measure in B
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getDecayConst(double time) {
			return properties[6].get(time);
		}

		/**
		 * Volume reduction per measure in B
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param decayConst
		 */
		public synchronized void setDecayConst(double decayConst) {
			final double t=Floats.D_TINY;
			properties[6].set(decayConst<t?t:decayConst);
		}

		/**
		 * Volume reduction per note length in B
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getDecayFrac(double time) {
			return properties[7].get(time);
		}

		/**
		 * Volume reduction per note length in B
		 * <br>
		 * Negative snaps to threshold
		 * <br>
		 * Part of envelope
		 * 
		 * @param decayFrac
		 */
		public synchronized void setDecayFrac(double decayFrac) {
			final double t=Floats.D_TINY;
			properties[8].set(decayFrac<t?t:decayFrac);
		}

		/**
		 * Threshold to kill the voice in B, so when the current
		 * volume is this much lower than the cap, kill the voice
		 * <br>
		 * 0 insta-kills
		 * <br>
		 * Part of envelope
		 * 
		 * @param time applicable if automated
		 * @return
		 * @see VarDouble
		 */
		public synchronized double getMinVolume(double time) {
			return properties[9].get(time);
		}

		/**
		 * Threshold to kill the voice in B, so when the current
		 * volume is this much lower than the cap, kill the voice
		 * <br>
		 * Positive snaps to threshold
		 * <br>
		 * 0 would insta-kill it
		 * <br>
		 * Part of envelope
		 * 
		 * @param minVolume
		 */
		public synchronized void setMinVolume(double minVolume) {
			final double t=Floats.D_TINY;
			properties[9].set(minVolume>t?t:minVolume);
		}
		
		/**
		 * Allow spawning from outside
		 * 
		 * @param pitch pitch as semitones from A4 (440Hz)
		 * @param start start time in seconds
		 * @param end end time in seconds
		 * @return a voice for this oscillator
		 */
		public OscVoice spawn(double pitch,double start,double end){
			return spawn(pitch,start,end,0);
		}
		
		/**
		 * Allow spawning from outside
		 * 
		 * @param pitch pitch as semitones from A4 (440Hz)
		 * @param start start time in seconds
		 * @param end end time in seconds
		 * @param volume overall volume offset in B
		 * @return a voice for this oscillator
		 */
		public OscVoice spawn(double pitch,double start,double end,double volume){
			return new Osc.OscVoice(pitch,start,end,volume);
		}
		
		@Override
		public Osc copy(int depth,Map<String,Object> options){
			int nextDepth = depth-1;
			int newType = type;
			VarDouble[] newProperties = properties;
			int nproperties = newProperties.length;
			newProperties = Arrays.copyOf(newProperties, nproperties);
			Collection<String> blacklist = (Collection<String>)options.get("blacklist");
			Collection<String> whitelist = (Collection<String>)options.get("whitelist");
			Map<String,Object> set = (Map<String,Object>)options.get("set");
			VarDouble dval;
			for(int i=0;i<nproperties;i++){
				final String FIELD_NAME = OSC_CLASS_NAME+OSC_PROPERTIES_NAMES[i];
				dval = (VarDouble) set.get(FIELD_NAME);
				if(dval!=null){
					newProperties[i] = dval;
				}else if(!BetterClone.fieldIncluded(blacklist, null,
								FIELD_NAME)
						|| BetterClone.fieldIncluded(whitelist, null,
								FIELD_NAME)){
					newProperties[i] = BetterClone.tryCopy(newProperties[i], depth-1, options);
				}
			}
			Number val;
			val = (Number) set.get(OSC_CLASS_NAME+".type");
			if(val!=null)newType = val.intValue();
			// make the copied object
			Osc result = new Osc();
			result.type = newType;
			return result;
		}
		
		/**
		 * A voice for this oscillator
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public class OscVoice implements Voice{
			
			/**
			 * One byte used to track the step
			 * <br>
			 * 0: Attack
			 * 1: Hold
			 * 2: Decay
			 * 3: Dead
			 */
			public byte step;
			/**
			 * Frequency pre-adjustment
			 */
			public double freq;
			/**
			 * Time pre-adjustment
			 */
			public double time;
			/**
			 * Phase (for consistent waveforms)
			 */
			public double phase;
			/**
			 * The time of switching to hold
			 */
			public double switched;
			/**
			 * Volume offset in B
			 */
			public double mult;
			/**
			 * Independent volume offset applied after in B
			 */
			public double multOver;
			/**
			 * Sample rate
			 */
			public int sampleRate;
			/**
			 * Seconds per sample
			 */
			public double sampleLength;
			/**
			 * Length of measure in seconds (average)
			 */
			public double measure;
			/**
			 * Length of note in seconds
			 */
			public double note;
			/**
			 * Delay in seconds
			 */
			public double delay;
			
			/**
			 * Destroyed yet?
			 */
			protected transient boolean destroyed = false;
			
			/**
			 * Fill in fields automatically
			 * 
			 * @param pitch pitch as semitones from A4 (440Hz)
			 * @param start start time in seconds
			 * @param end end time in seconds
			 */
			public OscVoice(double pitch,double start,double end){
				this(pitch,start,end,0);
			}
			
			/**
			 * Fill in fields automatically
			 * 
			 * @param pitch pitch as semitones from A4 (440Hz)
			 * @param start start time in seconds
			 * @param end end time in seconds
			 * @param volume overall volume offset
			 */
			public OscVoice(double pitch,double start,double end,double volume){
				note=end-start;
				measure=(parentComposition.secondsToMeasures(end)
						-parentComposition.secondsToMeasures(start))
						/note;
				sampleRate=parentComposition.currentSession.getSampleRate();
				sampleLength=1d/sampleRate;
				time=0d;
				phase=0d;
				step=0;
				mult=getMinVolume(time);
				multOver=volume;
				freq=440d*Math.pow(SEMITONE, pitch);
			}

			@Override
			public void destroy() {
				step = 3;
				destroyed = true;
			}

			@Override
			public void destroySelf() {
				step = 3;
				destroyed = true;
			}
			
			@Override
			public boolean isDestroyed(){
				return destroyed;
			}

			@Override
			public Samples nextSegment(int sampleCount) {
				double[] data = new double[sampleCount];
				if(step==3)return new Samples(sampleRate,data);
				int ltype = type;
				// Load values
				double ldetune = getDetune(time), lvolume = getVolume(time), lattackConst = getAttackConst(time), lattackFrac = getAttackFrac(time), lholdConst = getHoldConst(time),
						lholdFrac = getHoldFrac(time), ldecayConst = getDecayConst(time), ldecayFrac = getDecayFrac(time), lminVolume = getMinVolume(time);
				double afreq = freq*Math.pow(SEMITONE, ldetune), aattack = -lminVolume/(sampleRate*(lattackConst*measure+lattackFrac*note)),
						ahold = lholdConst*measure+lholdFrac*note, adecay = ldecayConst/measure+ldecayFrac/note, lpreMult = Math.pow(10d, lvolume+multOver);
				for(int i=0;step<3 && i<sampleCount;i++){
					if(delay>Floats.D_TINY){
						delay-=sampleLength;
						continue;
					}
					double wf = 0d;
					switch(ltype){
					case 0:{//Sine
						wf = PrimitiveWaveforms.unitSine(phase);
						break;
					}
					case 1:{//Square
						wf = PrimitiveWaveforms.unitSquare(phase);
						break;
					}
					case 2:{//Triangle
						wf = PrimitiveWaveforms.unitTriangle(phase);
						break;
					}
					case 3:{//Saw
						wf = PrimitiveWaveforms.unitSaw(phase);
						break;
					}
					}
					time += sampleLength;
					phase += sampleLength*afreq;
					switch(step){
					case 0:{
						double vol = lpreMult*Math.pow(10d, mult+=aattack);
						data[i] = wf*vol;
						if(mult>=lvolume){
							step=1;
							mult=lvolume;
							switched=time;
						}
						break;
					}
					case 1:{
						double vol = lpreMult;
						data[i] = wf*vol;
						if(time-switched>=ahold){//Not optimized away because the hold can change, and we like real time editing
							step=2;
						}
						break;
					}
					case 2:{
						double vol = lpreMult*Math.pow(10d, mult-=adecay);
						data[i] = wf*vol;
						if(mult<=lminVolume){
							step=3;
						}
						break;
					}
					}
				}
				return new Samples(sampleRate,data);
			}

			@Override
			public boolean isAlive() {
				return step!=3;
			}

			@Override
			public void requestKill() {
				if(step<2)step=2;
			}
			
		}
	}

}
