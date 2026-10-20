import { readFile } from 'node:fs/promises';
const base = (process.argv[2] || 'https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1').replace(/\/$/, '');
const url = new URL(base);
if (url.protocol !== 'https:' || !url.hostname.endsWith('.mockapi.io')) throw new Error('Expected a HTTPS MockAPI base URL');
async function request(path, method = 'GET', body) {
  const response = await fetch(`${base}/${path}`, {
    method, headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined, signal: AbortSignal.timeout(30000)
  });
  if (!response.ok) throw new Error(`${method} ${path}: HTTP ${response.status}. Create categories and courses resources first.`);
  return response.json();
}
const categories = await request('categories');
const courses = await request('courses');
if (!Array.isArray(categories) || !Array.isArray(courses)) throw new Error('Resources must return JSON arrays');
const seeds = JSON.parse(await readFile(new URL('../mockapi/categories.json', import.meta.url), 'utf8'));
const ids = {};
const keys = ['REPLACE_WITH_MOBILE_ID', 'REPLACE_WITH_WEB_ID', 'REPLACE_WITH_AI_ID'];
for (const [index, seed] of seeds.entries()) {
  const category = categories.find(item => item.name === seed.name) || await request('categories', 'POST', seed);
  ids[keys[index]] = String(category.id);
}
const courseSeeds = JSON.parse(await readFile(new URL('../mockapi/courses-template.json', import.meta.url), 'utf8'));
for (const seed of courseSeeds) {
  if (courses.some(item => item.title === seed.title)) continue;
  await request('courses', 'POST', { ...seed, categoryId: ids[seed.categoryId] });
  console.log(`Created: ${seed.title}`);
}
const finalCategories = await request('categories');
const finalCourses = await request('courses');
const categoryIds = new Set(finalCategories.map(item => String(item.id)));
if (finalCourses.some(item => !categoryIds.has(String(item.categoryId)))) throw new Error('Found a course referencing a missing category');
console.log(`Verified ${finalCategories.length} categories, ${finalCourses.length} courses. Existing records were not overwritten.`);
