import dbQuery from '../native/dbQuery.js';
export default function listPronunciationsSince(since) {
  return dbQuery('SELECT ts,score FROM pronunciations WHERE ts>=? ORDER BY ts', [since]);
}
