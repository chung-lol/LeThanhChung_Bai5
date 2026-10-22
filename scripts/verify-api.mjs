// Read-only verification; never creates, updates, or deletes backend records.
import assert from 'node:assert/strict';

const base = (process.argv[2] || 'https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1').replace(/\/+$/, '');
const parsed = new URL(base);
assert(parsed.protocol === 'https:' && parsed.hostname.endsWith('.mockapi.io'), 'Use a HTTPS MockAPI base URL');
async function get(path) {
  const response = await fetch(`${base}/${path}`, { signal: AbortSignal.timeout(30000) });
  assert(response.ok, `GET ${path}: HTTP ${response.status}`);
  return response.json();
}
const [categories, courses] = await Promise.all([get('categories'), get('courses')]);
assert(Array.isArray(categories) && categories.length >= 3, 'At least 3 categories required');
assert(Array.isArray(courses) && courses.length >= 10, 'At least 10 courses required');
const ids = new Set(categories.map(item => String(item.id)));
for (const category of categories) {
  assert(typeof category.name === 'string' && category.name.trim(), `Missing category name: ${category.id}`);
  assert(typeof category.description === 'string' && category.description.trim(), `Missing category description: ${category.id}`);
}
for (const course of courses) {
  for (const key of ['title', 'description', 'categoryId', 'instructor', 'thumbnail', 'trailerUrl', 'createdAt']) {
    assert(typeof course[key] === 'string' && course[key].trim(), `Course ${course.id}: invalid ${key}`);
  }
  assert(ids.has(course.categoryId), `Course ${course.id}: missing category`);
  assert(Number.isFinite(course.price) && course.price >= 0, `Course ${course.id}: invalid price`);
  assert(Number.isInteger(course.duration) && course.duration > 0, `Course ${course.id}: invalid duration`);
  assert(/^\d{4}-\d{2}-\d{2}$/.test(course.createdAt) &&
    new Date(course.createdAt).toISOString().slice(0, 10) === course.createdAt, `Course ${course.id}: invalid date`);
  for (const key of ['thumbnail', 'trailerUrl']) assert(new URL(course[key]).protocol === 'https:', `Invalid ${key}`);
}
const detail = await get(`courses/${encodeURIComponent(courses[0].id)}`);
assert.equal(detail.id, courses[0].id, 'Detail endpoint must return requested course');
const media = [...new Set(courses.flatMap(course => [course.thumbnail, course.trailerUrl]))];
for (const url of media) {
  const response = await fetch(url, { method: 'HEAD', signal: AbortSignal.timeout(30000) });
  assert(response.ok, `Media unavailable: HTTP ${response.status} ${url}`);
  assert(/^(image|video)\//.test(response.headers.get('content-type') || ''), `Expected image/video: ${url}`);
}
console.log(`PASS: ${categories.length} categories, ${courses.length} courses, valid references, detail endpoint, ${media.length} media URLs.`);
