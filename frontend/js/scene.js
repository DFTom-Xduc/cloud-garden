// A view of server data. This class never changes money or gameplay state.
export class FarmScene {
  constructor(canvas,onPick){this.zoom=1;this.pan={x:0,y:0};this.drag=null;this.suppressClick=false;this.herdSheet=new Image();this.herdSheet.src='assets/herd-atlas.png';this.herdSheet.onload=()=>{this.dirty=true;};this.quality='auto';this.low=Number(navigator.hardwareConcurrency||8)<=4;this.slowFrames=0;this.paused=false;this.dirty=true;this.markers=[];this.terrainCache=null;this.flowerCache=new Map();this.animalSheet=new Image();this.animalSheet.src='assets/livestock-atlas.png';this.animalSheet.onload=()=>{this.dirty=true;};this.canvas=canvas;this.g=canvas.getContext('2d');this.state=null;this.selected=null;this.hover=null;this.motionPreference=matchMedia('(prefers-reduced-motion: reduce)');this.reduced=this.motionPreference.matches;this.time=0;this.clockStart=performance.now();this.clockMinutes=480;this.motionPreference.addEventListener('change',e=>{this.reduced=e.matches;});
    this.resize=new ResizeObserver(()=>this.draw(performance.now()));this.resize.observe(canvas);
    canvas.addEventListener('pointerdown',e=>{if(e.button!==0)return;this.drag={x:e.clientX,y:e.clientY,px:this.pan.x,py:this.pan.y,moved:false};canvas.setPointerCapture(e.pointerId);});
    canvas.addEventListener('pointermove',e=>{if(this.drag){const dx=e.clientX-this.drag.x,dy=e.clientY-this.drag.y;if(Math.hypot(dx,dy)>5)this.drag.moved=true;if(this.drag.moved){const {s}=this.geometry();this.pan={x:this.drag.px+dx/s,y:this.drag.py+dy/s};this.viewChanged();return;}}const hit=this.hit(e);this.hover=hit?.animal?null:hit;canvas.classList.toggle('over-animal',Boolean(hit?.animal));this.dirty=true;});
    canvas.addEventListener('pointerup',e=>{this.suppressClick=Boolean(this.drag?.moved);this.drag=null;if(canvas.hasPointerCapture(e.pointerId))canvas.releasePointerCapture(e.pointerId);});
    canvas.addEventListener('pointercancel',()=>{this.drag=null;this.suppressClick=true;});
    canvas.addEventListener('click',e=>{if(this.suppressClick){this.suppressClick=false;return;}onPick(this.hit(e));});
    canvas.addEventListener('pointerleave',()=>{this.hover=null;canvas.classList.remove('over-animal');this.dirty=true;});
    canvas.addEventListener('wheel',e=>{e.preventDefault();this.setZoom(this.zoom*(e.deltaY<0?1.1:1/1.1));},{passive:false});
    let last=0;const frame=t=>{if(!document.hidden&&!this.paused&&(!this.reduced||this.dirty)&&t-last>=1000/(this.low?20:30)){const start=performance.now();this.draw(t);const elapsed=performance.now()-start;if(this.quality==='auto'&&!this.low){this.slowFrames=elapsed>24?this.slowFrames+1:Math.max(0,this.slowFrames-1);if(this.slowFrames>=12){this.low=true;this.canvas.parentElement.dataset.quality='low';}}this.dirty=false;last=t;}requestAnimationFrame(frame);};requestAnimationFrame(frame);}
  setQuality(value){this.quality=['auto','low','high'].includes(value)?value:'auto';this.low=this.quality==='low'||this.quality==='auto'&&Number(navigator.hardwareConcurrency||8)<=4;this.slowFrames=0;this.terrainCache=null;this.dirty=true;this.canvas.parentElement.dataset.quality=this.low?'low':'high';}
  update(state,selected){this.dirty=true;
    if(state.clock!==this.state?.clock||state.day!==this.state?.day){const [hour,minute]=state.clock.split(':').map(Number);this.clockMinutes=hour*60+minute;this.clockStart=performance.now();}
    this.state=state;this.selected=selected;
  }
  viewChanged(){this.dirty=true;this.draw(performance.now());this.onViewChange?.();}
  setZoom(value){this.zoom=Math.max(.65,Math.min(2.2,value));this.viewChanged();}
  resetView(){this.zoom=1;this.pan={x:0,y:0};this.viewChanged();}
  alertAnchor(id){const points={ga:[2.085,7.4,76],lon:[4.155,9.9,76],bo:[16.285,9.4,76],cuu:[22.1,13.5,172],de:[14,16.8,172]};const [u,v,height]=points[id];const [x,y]=this.p(u,v);return [x,y-height];}
  animalAlerts(){if(!this.state)return [];return this.state.animals.filter(a=>a.exists&&a.habitatBuilt&&(!a.fed||a.ready)).map(a=>{const [x,y]=this.toScreen(...this.alertAnchor(a.id));return {id:a.id,x,y};});}
  toScreen(x,y){const {s,lw,lh,z,cx,cy}=this.geometry();return [(lw/2+this.pan.x+(x-cx)*z)*s,(410+(lh-810)/2+this.pan.y+(y-cy)*z)*s];}
  fromScreen(x,y){const {s,lw,lh,z,cx,cy}=this.geometry();return [(x/s-lw/2-this.pan.x)/z+cx,(y/s-410-(lh-810)/2-this.pan.y)/z+cy];}
  // Every architectural face uses the island's isometric axes and a vertical height.
  project(x,y,z=0){return [x-y,(x+y)*16/33-z];}
  housePoint(x,y,z=0){const [px,py]=this.project(x,y,z);return [px+9,py+17-180*16/33];}
  surface(origin,horizontal,vertical,paint){const g=this.g;g.save();g.transform(...horizontal,...vertical,...origin);paint();g.restore();}
  sky(t,lw,lh){
    const g=this.g,seconds=this.time;
    const minute=(this.clockMinutes+(this.reduced?0:Math.min((t-this.clockStart)/1000,2)*10))%1440;
    const progress=this.state.night?(minute>=1080?(minute-1080)/720:(minute+360)/720):(minute-360)/720;
    const phase=Math.max(0,Math.min(1,progress));
    const x=lw*(.22+.59*phase),y=236-Math.sin(phase*Math.PI)*145;
    if(this.state.night){
      for(let i=0;i<48;i++){const radius=i%4?1.1:1.9;g.globalAlpha=.5+.45*(.5+.5*Math.sin(seconds*1.4+i*2.1));this.ellipse((i*197+71)%lw,(i*83+12)%Math.max(230,lh-70),radius,radius,'#ecf5ff');}g.globalAlpha=1;
    }
    const glow=g.createRadialGradient(x,y,10,x,y,95);glow.addColorStop(0,this.state.night?'#bcdfff55':'#ffe45a66');glow.addColorStop(1,this.state.night?'#bcdfff00':'#ffe45a00');this.ellipse(x,y,95,95,glow);
    g.save();g.translate(x,y);g.rotate(seconds/(this.state.night?75:28));
    if(this.state.night){this.ellipse(0,0,33,33,'#fff4ce');[[-13,-8,7],[8,12,6],[12,-14,4],[-12,15,3]].forEach(([a,b,r])=>this.ellipse(a,b,r,r,'#d8dbb5'));}
    else {for(let i=0;i<12;i++){const a=i*Math.PI/6,r=48+Math.sin(seconds*1.8)*2;this.line(Math.cos(a)*r,Math.sin(a)*r,Math.cos(a)*(r+9),Math.sin(a)*(r+9),'#ffcd32',3);}this.ellipse(0,0,37,37,'#ffd333');this.ellipse(-8,-8,23,23,'#ffe965');}g.restore();
    const clouds=[[.07,180,.85,.68,9],[.75,322,.92,.72,12],[.02,415,.86,.65,7],[.18,lh-170,1.1,.72,10],[.81,lh-174,.7,.68,14]];
    for(const [start,cy,size,alpha,speed] of clouds){const span=lw+340*size;const cx=((start*lw+seconds*speed+340*size)%span)-340*size;this.cloud(cx,cy+Math.sin(seconds*.24+start*8)*4,size,alpha);}
  }
  p(u,v){return [676+(u-v)*33,164+(u+v)*16];}
  poly(points,fill,stroke=null,width=1){const g=this.g;g.beginPath();points.forEach((p,i)=>i?g.lineTo(...p):g.moveTo(...p));g.closePath();if(fill){g.fillStyle=fill;g.fill();}if(stroke){g.strokeStyle=stroke;g.lineWidth=width;g.stroke();}}
  tile(u,v,w,d,fill,stroke=null){this.poly([this.p(u,v),this.p(u+w,v),this.p(u+w,v+d),this.p(u,v+d)],fill,stroke);}
  line(x,y,a,b,color,width=1){const g=this.g;g.beginPath();g.moveTo(x,y);g.lineTo(a,b);g.strokeStyle=color;g.lineWidth=width;g.stroke();}
  ellipse(x,y,rx,ry,fill){const g=this.g;g.beginPath();g.ellipse(x,y,rx,ry,0,0,Math.PI*2);g.fillStyle=fill;g.fill();}
  rect(x,y,w,h,color){this.g.fillStyle=color;this.g.fillRect(x,y,w,h);}
  at(u,v,paint){this.g.save();this.g.translate(...this.p(u,v));paint();this.g.restore();}
  geometry(){const w=this.canvas.clientWidth,h=this.canvas.clientHeight,s=Math.min(w/1440,h/810);return {w,h,s,lw:w/s,lh:h/s,z:(this.state?.expanded?.64:.8)*this.zoom,cx:676+((this.state?.worldWidth||22)-(this.state?.worldDepth||17))*16.5,cy:this.state?.expanded?560:500};}
  plotUV(p){return p.column<4?[7.4+p.column*1.65,6.2+p.row*1.7]:[3.8+(p.column-4)*1.65,13.5+p.row*1.7];}
  hit(e){if(!this.state)return null;const r=this.canvas.getBoundingClientRect(),[x,y]=this.fromScreen(e.clientX-r.left,e.clientY-r.top);
    for(const a of this.state.animals)if(a.exists&&a.habitatBuilt&&(!a.fed||a.ready)){const [mx,my]=this.alertAnchor(a.id);if(Math.hypot(x-mx,y-my)<20)return {animal:a.id};}
    const u=((x-676)/33+(y-164)/16)/2,v=((y-164)/16-(x-676)/33)/2;
    return this.state.plots.find(p=>{const [pu,pv]=this.plotUV(p);return u>=pu&&u<=pu+1.48&&v>=pv&&v<=pv+1.52;})||null;}
  cloud(x,y,s=1,alpha=.7){this.g.save();this.g.translate(x,y);this.g.scale(s,s);this.g.globalAlpha=alpha;const c=this.state?.night?'#bfcddc':'#fafff4';this.ellipse(55,33,70,21,c);this.ellipse(97,21,52,34,c);this.ellipse(163,31,68,28,c);this.ellipse(215,44,51,17,c);this.g.restore();}
  draw(t){const {w,h,s,lw,lh,z,cx,cy}=this.geometry(),g=this.g,dpr=Math.min(devicePixelRatio||1,this.low?1:1.5);if(!w||!h)return;
    if(this.canvas.width!==Math.round(w*dpr)||this.canvas.height!==Math.round(h*dpr)){this.canvas.width=Math.round(w*dpr);this.canvas.height=Math.round(h*dpr);}g.setTransform(dpr,0,0,dpr,0,0);g.clearRect(0,0,w,h);g.scale(s,s);if(!this.state)return;
    this.time=this.reduced?0:t/1000;this.sky(t,lw,lh);
    g.save();g.translate(lw/2+this.pan.x,410+(lh-810)/2+this.pan.y);g.scale(z,z);g.translate(-cx,-cy);this.ellipse(738,637,430,70,'#477c491c');this.cloud(480+Math.sin(this.time*.13)*22,600,1.1,.76);
    const width=this.state.worldWidth||22,depth=this.state.worldDepth||17;
    this.paintTerrain(width,depth);this.markers=[];
    const items=[],add=(depth,paint)=>items.push({depth,paint});add(7.8,()=>this.house(4,3.8));add(14.6,()=>this.house(11.5,3.1,true));add(12.1,()=>this.windmill(1.5,10.6,t));add(20,()=>this.tractor(14.5,5.5));add(10.3,()=>this.enclosure('ga',.6,6,2.7,2.8));add(14,()=>this.enclosure('lon',3,8,2.1,3.8));add(26,()=>this.enclosure('bo',14.8,7,2.7,4.8));if(this.owned('COTTAGE'))add(23.1,()=>this.house(19.8,3.3));if(this.owned('SHEEP_BARN'))add(38,()=>this.pasture('cuu'));if(this.owned('GOAT_BARN'))add(34,()=>this.pasture('de'));
    [[1,1.4],[3,.8],[7,.8],[15.5,1.1],[17,3]].forEach(([u,v],i)=>add(u+v,()=>this.tree(u,v,i%3)));this.state.plots.forEach(p=>{const [u,v]=this.plotUV(p);add(u+v+1.52,()=>this.crop(p));});items.sort((a,b)=>a.depth-b.depth).forEach(i=>i.paint());
    for(let i=0;i<width;i++)this.fence(i,depth-.2,i+1,depth-.2);for(let i=0;i<depth;i++)this.fence(width-.2,i,width-.2,i+1);
    if(this.state.night){this.light(4,3.8);if(this.owned('COTTAGE'))this.light(19.8,3.3);}g.restore();}
  paintTerrain(width,depth){
    const key=width+':'+depth+':'+this.low;if(this.terrainCache?.key!==key){
      const canvas=document.createElement('canvas');canvas.width=1800;canvas.height=1100;
      const original=this.g;this.g=canvas.getContext('2d');
    const a=this.p(0,0),b=this.p(width,0),c=this.p(width,depth),d=this.p(0,depth);this.poly([d,c,[c[0],c[1]+55],[d[0],d[1]+55]],'#a77950');this.poly([b,c,[c[0],c[1]+55],[b[0],b[1]+55]],'#77604a');this.poly([a,b,c,d],'#438b34');
    for(let u=0;u<width;u++)for(let v=0;v<depth;v++){const n=(u*17+v*23)%17;this.tile(u+.026,v+.026,.95,.95,`rgb(${96+n},${180+n/2},${47+n/2})`);}
    for(let i=0;i<(this.low?220:650);i++){const p=this.p(((i*73)%997)/997*(width-.2)+.1,((i*97)%991)/991*(depth-.2)+.1);this.line(p[0],p[1],p[0]+2,p[1]-3,i%4?'#48933188':'#d5dc98');if(i%29===0)this.ellipse(p[0]+3,p[1]-2,1.7,1.7,'#f7edb7');}
    this.tile(5.2,.6,1.5,11.8,'#dfb15f');this.tile(1.4,4.3,15.2,1.5,'#edc97e');this.tile(13.9,5,1.4,6.5,'#e2b265');if(this.state.expanded){this.tile(17.8,4.3,6.5,1.5,'#edc97e');this.tile(18.5,5.8,1.2,9.7,'#edc97e');this.tile(6.7,13,11.8,1.2,'#edc97e');}
    for(let i=0;i<55;i++){const p=this.p(1.6+(i*43%97)/97*14.7,4.5+(i*73%89)/89*1.1);this.ellipse(...p,2,1,'#b8995c66');}for(let i=0;i<width;i++)this.fence(i,0,i+1,0);for(let i=0;i<5;i++)this.fence(0,i,0,i+1);

      this.g=original;this.terrainCache={key,canvas};
    }
    this.g.drawImage(this.terrainCache.canvas,0,0);
  }
  owned(id){return this.state.buildings.some(b=>b.id===id&&b.owned);}
  fence(u,v,a,b){const p=this.p(u,v),q=this.p(a,b);this.line(p[0],p[1]-18,q[0],q[1]-18,'#866c43',5);this.line(p[0],p[1]-19,q[0],q[1]-19,'#eed5a1',3);this.line(p[0],p[1]-7,q[0],q[1]-7,'#eed5a1',4);this.line(p[0],p[1]+4,p[0],p[1]-30,'#b7965c',6);this.line(p[0]-1,p[1]+2,p[0]-1,p[1]-30,'#f1d7a3',3);}
  window(w,h){
    const night=this.state.night;
    this.rect(-4,-4,w+8,h+8,'#ffe3a5');this.rect(0,0,w,h,night?'#ffb838':'#258e9f');
    this.rect(3,3,w/2-5,h-6,night?'#fff29a':'#8be4df');this.rect(w/2-2,0,4,h,'#ffe3a5');this.rect(0,h/2-2,w,4,'#ffe3a5');
  }
  house(u,v,barn=false,herdId=null){this.at(u,v,()=>{
    const q=(x,y,z=0)=>this.housePoint(x,y,z),k=16/33,h=90,rise=58,roofZ=x=>h+rise*(1-Math.abs(x-45)/45),ink='#83542d';
    this.ellipse(8,14,111,27,'#3c582f32');
    this.poly([q(-7,97),q(97,97),q(97,-7),q(-7,-7)],'#cbb584',ink);
    // The gable's base is exactly the wall's top edge; the ridge belongs to both faces.
    this.poly([q(0,90),q(90,90),q(90,90,h),q(0,90,h)],barn?'#c95035':'#d99249',ink);
    this.poly([q(90,90),q(90,0),q(90,0,h),q(90,90,h)],barn?'#ed7843':'#f0b164',ink);
    for(let z=13;z<h;z+=14){this.poly([q(1,90,z),q(89,90,z)],null,barn?'#963e2c':'#a96d35');this.poly([q(90,89,z),q(90,1,z)],null,barn?'#b64c31':'#c78b45');}
    [[0,90],[90,90],[90,0]].forEach(([x,y])=>{const a=q(x,y,0),b=q(x,y,h);this.line(...a,...b,'#ffe0a0',5);});
    this.poly([q(0,90,h),q(45,90,h+rise),q(90,90,h)],'#ffdf92',ink);
    for(let x=8;x<86;x+=10){const top=h+rise*(1-Math.abs(x-45)/45)-5;if(top>h)this.poly([q(x,90,h+3),q(x,90,top)],null,'#eac277');}
    // Project roof texture within the sloped plane, rather than using screen coordinates.
    const roof=[q(45,90,h+rise),q(45,-8,h+rise),q(98,-8,roofZ(98)),q(98,90,roofZ(98))];
    this.poly(roof,barn?'#276e65':'#168cbc',barn?'#20564f':'#12648c',1.5);
    this.g.save();this.poly(roof,null);this.g.clip();
    this.surface(q(45,90,h+rise),[1,-k],[1,k+rise/45],()=>{
      for(let row=0;row<7;row++)for(let col=-1;col<7;col++){const x=col*18+(row%2)*9,y=row*8;this.g.beginPath();this.g.ellipse(x+9,y+3,8,3,0,0,Math.PI);this.g.strokeStyle=barn?'#83c6a466':'#8ae3f366';this.g.lineWidth=.8;this.g.stroke();}
    });this.g.restore();
    this.poly([q(-8,90,roofZ(-8)),q(45,90,h+rise),q(98,90,roofZ(98))],null,'#ffe5a4',5);
    this.poly([q(98,90,roofZ(98)),q(98,-8,roofZ(98))],null,'#ffd58c',5);
    this.poly([q(45,90,h+rise),q(45,-8,h+rise)],null,barn?'#439889':'#49bcdf',2);
    if(barn){
      const open=herdId==='de'&&!this.state.night||herdId==='cuu'&&['LEAVING','RETURNING'].includes(this.state.herd?.mode);
      this.surface(q(90,67,69),[1,-k],[0,1],()=>{this.rect(-3,-3,49,65,'#ffe0a0');this.rect(0,0,43,59,open?'#352e25':'#6e4829');if(open){this.rect(0,0,5,59,'#c77d45');this.rect(38,0,5,59,'#c77d45');this.rect(5,52,33,7,'#aa9262');}else{this.line(2,3,41,56,'#ffe0a0',3);this.line(41,3,2,56,'#ffe0a0',3);this.rect(20,0,3,59,'#ffe0a0');}});
    }else{
      // Both windows use the same facade axes, including the flower box.
      this.surface(q(29,90,117),[1,k],[0,1],()=>this.window(28,21));
      this.surface(q(29,90,72),[1,k],[0,1],()=>{this.window(28,31);this.rect(-7,36,42,12,'#389047');for(let i=0;i<6;i++)this.ellipse(-3+i*7,35,3.5,3.5,i%2?'#ffe45b':'#fa799f');});
      this.surface(q(90,63,63),[1,-k],[0,1],()=>{this.rect(-2,-2,33,57,'#ffe3a5');this.rect(0,0,29,53,'#8e552c');this.ellipse(23,29,2,2,'#ffd33e');});
      this.poly([q(90,66),q(90,27),q(102,27),q(102,66)],'#ffe0a0','#c19352');
      this.poly([q(102,66,-4),q(102,27,-4),q(111,27,-4),q(111,66,-4)],'#d7a660','#b67e3d');
      const x=66,y=24,base=roofZ(x+10)-3,top=163;
      this.poly([q(x,y+12,base),q(x+10,y+12,base),q(x+10,y+12,top),q(x,y+12,top)],'#cb7943');
      this.poly([q(x+10,y+12,base),q(x+10,y,base),q(x+10,y,top),q(x+10,y+12,top)],'#ad5a33');
      this.poly([q(x-3,y+15,top),q(x+13,y+15,top),q(x+13,y-3,top),q(x-3,y-3,top)],'#ffbc76','#cb7943');
    }
  });}
  light(u,v){this.at(u,v,()=>{
    const q=(x,y,z)=>this.housePoint(x,y,z);
    for(const z of [117,72]){const [x,y]=q(43,90,z-15),gr=this.g.createRadialGradient(x,y,0,x,y,43);gr.addColorStop(0,'#ffd45466');gr.addColorStop(1,'#ffd45400');this.ellipse(x,y,43,43,gr);this.surface(q(29,90,z),[1,16/33],[0,1],()=>this.window(28,z===117?21:31));}
  });}
  tree(u,v,kind){this.at(u,v,()=>{
    this.ellipse(5,5,38,12,'#35642f2b');const phase=u*1.7+v*.9,sway=Math.sin(this.time*1.35+phase)*.033+Math.sin(this.time*.7+phase)*.012;
    this.g.save();this.g.transform(1,0,this.reduced?0:sway,1,0,0);
    this.poly([[-7,0],[-5,-74],[6,-75],[8,0],[17,6],[1,2],[-15,9]],'#9e6336');
    if(kind===1){for(let i=0;i<4;i++){const y=-119+i*22,w=23+i*9;this.poly([[0,y],[-w,y+46],[w,y+46]],['#237846','#299447','#3cac45','#62bf48'][i]);this.line(0,y+6,-w+7,y+40,'#99d46b');}}
    else {const colors=kind===2?['#ed851e','#ffa524','#ffd052']:['#38953c','#63b942','#9dd74c'];for(let i=0;i<9;i++){const a=i*2.4,flutter=this.reduced?0:Math.sin(this.time*1.8+phase+i)*1.4;this.ellipse(Math.cos(a)*23+flutter,Math.sin(a)*20-77,25,24,colors[i%3]);}if(kind===0)for(let i=0;i<5;i++)this.ellipse(-23+i*12,-71-i%2*21,4,5,'#f26944');}
    this.g.restore();
  });}
  crop(p){const [u,v]=this.plotUV(p);this.tile(u,v,1.48,1.52,'#94704b','#70583d');for(let k=0;k<5;k++){const a=this.p(u+.12,v+.15+k*.28),b=this.p(u+1.32,v+.15+k*.28);this.line(...a,...b,'#ba9160',3);}const f=this.state.flowers.find(f=>f.id===p.flowerId);if(f)for(let r=0;r<3;r++)for(let c=0;c<3;c++)this.flower(...this.p(u+.28+c*.43,v+.28+r*.43),f,p.level);
    if(p.cooldown>0)this.ellipse(...this.p(u+.7,v+.8),15,5,'#ade0e16b');if(this.selected?.row===p.row&&this.selected?.column===p.column||this.hover?.row===p.row&&this.hover?.column===p.column)this.poly([this.p(u,v),this.p(u+1.48,v),this.p(u+1.48,v+1.52),this.p(u,v+1.52)],null,'#fff2b6',3);}
  flower(x,y,f,level){
    const key=f.id+':'+level+':'+f.color;let art=this.flowerCache.get(key);
    if(!art){art=document.createElement('canvas');art.width=80;art.height=100;const original=this.g;this.g=art.getContext('2d');this.drawFlower(40,86,f,level);this.g=original;this.flowerCache.set(key,art);}
    this.g.drawImage(art,x-40,y-86);
  }
  drawFlower(x,y,f,level){const scale=1+(level-1)*.22,height=15+level*6;this.g.save();this.g.translate(x,y);this.g.scale(scale,scale);this.line(0,0,0,-height,'#4d7950',2);this.ellipse(-3,-8,4,2,'#6e9658');this.ellipse(4,-12,4,2,'#90ad63');if(f.id==='lavender'){for(let i=0;i<4+level;i++)this.ellipse(i%2?2:-2,-height+i*3,3,3,f.color);}else{const size=f.id==='sunflower'?4:3;for(let i=0;i<6;i++)this.ellipse(Math.cos(i*Math.PI/3)*size,-height+Math.sin(i*Math.PI/3)*size,size,size,f.color);this.ellipse(0,-height,2.6,2.6,f.id==='sunflower'?'#886740':'#d9b366');}if(level===3){this.ellipse(8,-height-6,1.5,1.5,'#fff5b0');this.ellipse(-8,-height+2,1,1,'#fff5b0');}this.g.restore();}
  tractor(u,v){this.at(u,v,()=>{this.ellipse(5,10,61,15,'#53663c36');this.wheel(-26,-5,22);this.wheel(48,-11,15);this.poly([[-42,-18],[18,-37],[58,-24],[-3,-3]],'#a06448','#576148');this.poly([[-5,-28],[44,-46],[57,-37],[7,-18]],'#db956a');this.poly([[7,-18],[57,-37],[57,-18],[7,1]],'#bd7954');this.poly([[-36,-62],[-5,-73],[12,-62],[-21,-50],[-21,-20],[-36,-28]],'#628c81','#e7cfa2');this.poly([[-18,-49],[7,-59],[7,-33],[-18,-23]],'#b4d6c7','#eed3a0');this.poly([[-44,-64],[-4,-79],[19,-67],[-23,-53]],'#dcad75','#776044');this.rect(34,-66,6,28,'#626751');this.ellipse(51,-28,3.5,4,'#f3d995');this.wheel(-10,7,23);this.wheel(57,-5,14);});}
  wheel(x,y,r){this.ellipse(x,y,r,r,'#47594c');this.ellipse(x,y,r-4,r-4,'#7c8267');this.ellipse(x,y,r/2,r/2,'#e2c18c');this.ellipse(x,y,3,3,'#a18357');}
  windmill(u,v,t){this.at(u,v,()=>{
    const q=(x,y,z=0)=>{const [px,py]=this.project(x,y,z);return [px,py+3-64*16/33];};
    this.ellipse(2,5,40,13,'#34683030');
    this.poly([q(0,32),q(32,32),q(25,25,105),q(7,25,105)],'#f4cf83','#9d753d');
    this.poly([q(32,32),q(32,0),q(25,7,105),q(25,25,105)],'#d8a85e','#9d753d');
    this.poly([q(3,29,105),q(16,16,140),q(29,29,105)],'#408c78','#2a6b5b');
    this.poly([q(16,16,140),q(29,3,105),q(29,29,105)],'#27715e','#215e50');
    this.poly([q(10,31,0),q(22,31,0),q(21,29,31),q(11,29,31)],'#98613b','#fce0a1');
    const hub=q(16,26.3,84),shaft=q(16,33.3,84);this.line(...hub,...shaft,'#78552e',7);
    // Rotation takes place in the front facade's plane, then gets projected onto the canvas.
    this.surface(shaft,[1,16/33],[0,1],()=>{
      this.g.rotate(this.reduced?-.7:t/2600);
      for(let k=0;k<4;k++){this.g.save();this.g.rotate(k*Math.PI/2);this.rect(3,-5,64,11,'#d29e51');this.rect(9,-7,60,10,'#ffe6a7');for(let x=14;x<68;x+=8)this.line(x,-7,x,3,'#bd8d49',1.3);this.line(8,-1,69,-1,'#f5ce81',1);this.g.restore();}
      this.ellipse(0,0,9,9,'#a4773c');this.ellipse(-2,-2,5,5,'#f4d38f');
    });
  });}
  enclosure(id,u,v,w,d){
    const a=this.state.animals.find(a=>a.id===id);if(!a?.habitatBuilt)return;
    this.tile(u,v,w,d,id==='lon'?'#987655':id==='bo'?'#80bd4d':'#e4bc79','#95784e');
    if(id==='lon')for(let i=0;i<8;i++)this.ellipse(...this.p(u+.3+i%3*.6,v+.4+Math.floor(i/3)*.9),14,6,'#76573e');
    // Open enclosures: no tiny detached huts blocking the tractor or animals.
    for(let i=0;i<Math.ceil(w);i++)this.fence(u+i,v,u+Math.min(i+1,w),v);
    this.at(u+w*.5,v+.65,()=>{this.poly([[-18,0],[2,-9],[22,0],[2,9]],'#997147','#705334');this.poly([[-15,-2],[2,-9],[19,-2],[2,5]],a.fed?'#d7b64b':'#725634');});
    if(a.exists){
      const n=id==='ga'?3:1;
      for(let i=0;i<n;i++)this.at(u+w*.45+(i%2)*.75,v+d*.52+Math.floor(i/2)*.8,()=>this.animal(id,a,i));
      this.careMarker(id,a);
    }
    for(let i=0;i<Math.ceil(w);i++)this.fence(u+i,v+d,u+Math.min(i+1,w),v+d);
    for(let i=0;i<Math.ceil(d);i++)this.fence(u+w,v+i,u+w,v+Math.min(i+1,d));
  }
  careMarker(id,a){if(!a.exists||a.fed&&!a.ready)return;const [x,y]=this.alertAnchor(id);this.markers.push({id,x,y});this.g.save();this.g.translate(x,y);this.ellipse(0,0,15,15,'#fff4c5');this.ellipse(0,0,12,12,a.ready?'#ebbd47':'#ee8653');this.g.fillStyle='#fffbed';this.g.font='bold 19px Arial';this.g.textAlign='center';this.g.fillText('!',0,7);this.g.restore();}
  herdFraction(){
    if(!this.state.dog?.exists)return 0;
    const m=(this.clockMinutes+(this.reduced?0:Math.min((performance.now()-this.clockStart)/1000,2)*10))%1440;
    return m<360?0:m<1080?Math.min(1,(m-360)/50):Math.max(0,1-(m-1080)/50);
  }
  herdPosition(index,fraction=this.herdFraction()){
    const door=[22.77,13.08],target=[18.7+index*.66,15.5+(index%2)*.65];
    const wander=fraction===1&&!this.reduced?Math.sin(this.time*.6+index*2)*.2:0;
    // Walk out through the east door and around the barn, never through its walls.
    const path=[door,[23.3+index*.22,14.6+index*.35],[21+index*.35,16.2+(index%2)*.25],target],segment=fraction<.25?0:fraction<.55?1:2,starts=[0,.25,.55],ends=[.25,.55,1];
    const t=(fraction-starts[segment])/(ends[segment]-starts[segment]),a=path[segment],b=path[segment+1];
    return [a[0]+(b[0]-a[0])*t+wander,a[1]+(b[1]-a[1])*t+Math.sin(this.time*.5+index)*wander];
  }
  pasture(id){
    const sheep=id==='cuu',a=this.state.animals.find(a=>a.id===id),u=sheep?17.8:9,v=sheep?10.9:14.3,w=sheep?6.1:6.3,d=sheep?6:4.9;
    this.tile(u,v,w,d,sheep?'#86c856':'#7abf45','#62973f');
    for(let i=0;i<Math.ceil(w);i++)this.fence(u+i,v,u+Math.min(i+1,w),v);
    this.house(sheep?22.1:14,sheep?13.5:16.8,true,id);
    const moving=sheep&&['LEAVING','RETURNING'].includes(this.state.herd?.mode),f=sheep?this.herdFraction():this.state.night?0:1;
    if(a?.exists&&f>.02){
      const points=Array.from({length:sheep?3:2},(_,i)=>{const phase=this.reduced?0:this.time*.5+i*2;const uv=sheep?this.herdPosition(i,f):[10.1+i*.85+Math.sin(phase)*.32,17.8+(i%2)*.55+Math.cos(phase)*.24];return {i,uv};}).sort((a,b)=>a.uv[0]+a.uv[1]-b.uv[0]-b.uv[1]);
      for(const {i,uv} of points)this.at(...uv,()=>this.animal(id,moving?{...a,fed:true,ready:false}:a,i));
    }
    if(this.state.dog?.exists){const duty=this.state.herd?.duty,herding=duty==='HERD_OUT'||duty==='HERD_IN';if(sheep&&herding){const p=this.herdPosition(1,f);this.at(p[0]+.25,Math.min(16.6,p[1]+.4),()=>this.animal('dog',{fed:true,ready:false}));}else if(!sheep&&!herding){const t=this.reduced?0:this.time*.23;this.at(10.5+Math.cos(t)*.85,16.7+Math.sin(t)*1.5,()=>this.animal('dog',{fed:true,ready:false}));}else if(sheep&&!this.owned('GOAT_BARN')&&!herding)this.at(21,15,()=>this.animal('dog',{fed:true,ready:false}));}
    for(let i=0;i<Math.ceil(w);i++)this.fence(u+i,v+d,u+Math.min(i+1,w),v+d);
    for(let i=0;i<Math.ceil(d);i++)this.fence(u+w,v+i,u+w,v+Math.min(i+1,d));
    this.at(u+.2,v+d,()=>{this.rect(-19,-39,89,19,'#fff0c9');this.g.fillStyle='#315737';this.g.font='bold 12px Arial';this.g.textAlign='left';this.g.fillText(sheep?(this.state.english?'SHEEP PASTURE':'ĐỒNG CỎ CỪU'):(this.state.english?'GOAT FARM':'TRANG TRẠI DÊ'),-15,-26);});
    this.careMarker(id,a);
  }
  animal(id,a,index=0){
    const g=this.g,active=a.fed&&!a.ready&&!this.reduced,t=this.time*3.2+index*1.8,step=active?Math.sin(t)*2:0;
    this.ellipse(0,3,id==='ga'?13:29,id==='ga'?5:9,'#304d372c');g.save();
    g.translate(active?Math.sin(t*.45)*5:0,-Math.abs(step));g.rotate(active?Math.sin(t)*.055:0);
    const image=['cuu','de','dog'].includes(id)?this.herdSheet:this.animalSheet;if(image.complete&&image.naturalWidth){
      const cell=image.naturalWidth/3,slot={ga:0,bo:1,lon:2,cuu:0,de:1,dog:2}[id],size=id==='ga'?48:id==='bo'?86:id==='dog'?62:74;
      g.drawImage(image,slot*cell,0,cell,image.naturalHeight,-size/2,-size*.86,size,size);
    }else{this.ellipse(0,-18,id==='ga'?13:27,id==='ga'?12:18,id==='ga'?'#a9572d':id==='bo'?'#faf3dc':'#f6b5b0');this.ellipse(id==='ga'?-10:20,-26,9,9,id==='ga'?'#c97438':id==='bo'?'#292c29':'#ef9ca2');}
    if(active){for(let i=0;i<3;i++)this.ellipse((id==='bo'?27:-18)+i*3,-13+Math.sin(t+i)*3,1.5,1.5,id==='bo'?'#68bc4a':'#ffd75e');}
    g.restore();
  }
}
