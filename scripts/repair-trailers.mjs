// Replace only the known unavailable sample trailer; preserve custom URLs and all other fields.
const base = (process.argv[2] || 'https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1').replace(/\/+$/, '');
const parsed = new URL(base);
if (parsed.protocol !== 'https:' || !parsed.hostname.endsWith('.mockapi.io')) throw new Error('Expected HTTPS MockAPI base URL');
const oldUrl = 'https://media.w3.org/2010/05/sintel/trailer.mp4';
const newUrl = 'https://www.w3schools.com/html/mov_bbb.mp4';
const check = await fetch(newUrl, { headers: { Range: 'bytes=0-1023' }, signal: AbortSignal.timeout(30000) });
if (!check.ok || !check.headers.get('content-type')?.startsWith('video/')) throw new Error('Replacement video unavailable');
await check.body.cancel();
const response = await fetch(`${base}/courses`, { signal: AbortSignal.timeout(30000) });
if (!response.ok) throw new Error(`GET courses: ${response.status}`);
const courses = await response.json();
for (const course of courses.filter(item => item.trailerUrl === oldUrl)) {
  const result = await fetch(`${base}/courses/${encodeURIComponent(course.id)}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ trailerUrl: newUrl }), signal: AbortSignal.timeout(30000)
  });
  if (!result.ok) throw new Error(`PUT ${course.id}: ${result.status}`);
  console.log(`Updated trailer: ${course.id}`);
}
