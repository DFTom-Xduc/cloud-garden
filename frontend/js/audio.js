export class FarmAudio {
  constructor(){this.music=35;this.sfx=65;this.started=false;}
  configure(music,sfx){this.music=music;this.sfx=sfx;if(this.gain)this.gain.gain.setTargetAtTime(music/100*.035,this.context.currentTime,.15);}
  start(){if(this.started){this.context?.resume();return;}try{this.context=new AudioContext();this.gain=this.context.createGain();this.gain.gain.value=this.music/100*.035;this.gain.connect(this.context.destination);this.started=true;this.context.resume();let i=0;const notes=[60,64,67,72,67,64,62,65,69,74,69,65,64,67,71,76];this.timer=setInterval(()=>{if(this.music>0&&document.visibilityState==='visible')this.note(notes[i++%notes.length],.8,this.gain);},700);}catch{/* The garden remains playable on devices without Web Audio. */}}
  note(midi,duration,target){const c=this.context,o=c.createOscillator(),e=c.createGain(),now=c.currentTime;o.type='sine';o.frequency.value=440*2**((midi-69)/12);e.gain.setValueAtTime(0,now);e.gain.linearRampToValueAtTime(1,now+.02);e.gain.exponentialRampToValueAtTime(.001,now+duration);o.connect(e);e.connect(target);o.start(now);o.stop(now+duration);o.onended=()=>{o.disconnect();e.disconnect();};}
  click(){if(!this.started||!this.sfx)return;const v=this.context.createGain();v.gain.value=this.sfx/100*.045;v.connect(this.context.destination);this.note(79,.12,v);setTimeout(()=>v.disconnect(),300);}
}
