const shapes={
  mail:'<rect x="3" y="6" width="26" height="20" rx="3" fill="#eed291"/><path d="m4 8 12 10L28 8"/>',
  settings:'<path d="m13 2-1 4-4 2-4-1-2 5 3 3v4l-3 3 3 5 4-1 4 2 1 4h6l1-4 4-2 4 1 3-5-3-3v-4l3-3-3-5-4 1-4-2-1-4Z" transform="translate(0 -1) scale(.96)"/><circle cx="16" cy="16" r="5"/>',
  home:'<path d="m2 15 14-12 14 12M6 13v16h20V13"/><path d="M13 29V19h6v10" fill="#d9be76"/>',
  house:'<path d="m2 14 14-11 14 11" fill="#698153"/><path d="M6 14v15h20V14" fill="#dcbd7e"/><path d="M14 29V20h5v9"/>',
  pointer:'<path d="M6 2v26l8-7 5 10 5-3-6-9 11-1Z" fill="#617750" stroke="none"/>',
  leaf:'<path d="M16 29V8"/><ellipse cx="9" cy="17" rx="7" ry="4" fill="#78965d" stroke="none" transform="rotate(30 9 17)"/><ellipse cx="23" cy="9" rx="7" ry="4" fill="#597e4f" stroke="none" transform="rotate(-25 23 9)"/>',
  shovel:'<path d="M6 20 20 6" stroke="#f2a75f" stroke-width="5"/><path d="m18 4 4-3 9 9-3 4Z" stroke="#96a1a8" stroke-width="3"/><path d="m4 15 13 13C9 37-5 25 4 15Z" fill="#99a6ad" stroke="#82939b"/>',
  water:'<path d="M11 12V7c0-9 13-7 13 1v6" stroke="#279d99" stroke-width="3"/><path d="m8 18-7-5v6l8 6" fill="#1aafa9" stroke="none"/><rect x="7" y="12" width="20" height="17" rx="5" fill="#1bc5ba" stroke="none"/><path d="m2 22-1 3m3-1-1 4" stroke="#68d8e2"/>',
  coin:'<circle cx="16" cy="16" r="14" fill="#d6a745" stroke="none"/><circle cx="15" cy="14" r="12" fill="#f1d379" stroke="#ffeaa0"/><path d="M15 6v17m4-14c-8-5-12 4-4 5s4 10-4 5" stroke="#b08539"/>',
  diamond:'<path d="M2 11 8 3h16l6 8-14 19Z" fill="#a9d5d7" stroke="#709fac"/><path d="M2 11h28M8 3l4 8 4 19 4-19 4-8" stroke="#e1f2ea"/>',
  sun:'<circle cx="16" cy="16" r="6" fill="#d7b866" stroke="none"/><path d="M16 2v4m0 20v4M2 16h4m20 0h4M6 6l3 3m14 14 3 3M6 26l3-3M23 9l3-3" stroke="#b99c54"/>',
  moon:'<path d="M24 4a13 13 0 1 0 5 20A15 15 0 0 1 24 4Z" fill="#d7c589" stroke="none"/>',
  chicken:'<ellipse cx="13" cy="22" rx="11" ry="9" fill="#e2c582" stroke="none"/><circle cx="23" cy="12" r="7" fill="#e2c582" stroke="none"/><path d="m28 12 4 3-4 2" fill="#c89944" stroke="none"/><circle cx="25" cy="10" r="1" fill="#51674b" stroke="none"/><path d="M22 5V2m4 4V3" stroke="#c57553" stroke-width="3"/>',
  egg:'<path d="M16 2C10 2 4 17 4 23a12 12 0 0 0 24 0C28 17 22 2 16 2Z" fill="#f2e6c3" stroke="#c9b99a"/>',
  expand:'<path d="M3 12V3h9M20 3h9v9M29 20v9h-9M12 29H3v-9M4 4l7 7m10 10 7 7M4 28l7-7m10-10 7-7"/>',
  check:'<path d="m6 17 6 6L27 8"/>',
  grid:'<rect x="3" y="3" width="10" height="10" rx="2"/><rect x="19" y="3" width="10" height="10" rx="2"/><rect x="3" y="19" width="10" height="10" rx="2"/><rect x="19" y="19" width="10" height="10" rx="2"/>'
};
export function icon(name){return `<svg viewBox="0 0 32 32" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${shapes[name]||shapes.leaf}</svg>`;}
export function flowerArt(f){return `<svg viewBox="0 0 100 120" aria-hidden="true"><path d="M50 113V40" stroke="#5c8150" stroke-width="5"/><ellipse cx="37" cy="80" rx="15" ry="7" fill="#8ba962" transform="rotate(27 37 80)"/><ellipse cx="64" cy="69" rx="15" ry="7" fill="#729551" transform="rotate(-25 64 69)"/>${f.id==='lavender'?Array.from({length:6},(_,i)=>`<ellipse cx="${49+i%2*4}" cy="${49-i*6}" rx="10" ry="5" fill="${f.color}"/>`).join(''):Array.from({length:7},(_,i)=>`<ellipse cx="${50+Math.cos(i*Math.PI*2/7)*15}" cy="${35+Math.sin(i*Math.PI*2/7)*15}" rx="11" ry="13" fill="${f.color}"/>`).join('')+`<circle cx="50" cy="35" r="9" fill="${f.id==='sunflower'?'#8b6742':'#dfb762'}"/>`}<circle cx="24" cy="29" r="2" fill="#dfc980"/><circle cx="77" cy="47" r="2" fill="#dfc980"/></svg>`;}
export function animalArt(id){return `<span class="livestock-art livestock-${id}" role="img" aria-label="${({ga:'Gà',bo:'Bò',lon:'Lợn',cuu:'Cừu',de:'Dê',dog:'Chó canh gác'})[id]||id}"></span>`;}
