import Head from 'next/head';
import { useState } from 'react';
import SearchBar from '../components/SearchBar';
import ResultItem from '../components/ResultItem';

export default function Home() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (query) => {
    if (!query) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080'}/search?q=${encodeURIComponent(query)}`);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }
      const data = await res.json();
      setResults(data);
    } catch (e) {
      console.error(e);
      setError('Failed to fetch results');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Head>
        <title>Distributed Search Engine</title>
        <meta name="description" content="Search the web with a distributed engine" />
      </Head>
      <main style={styles.main}>
        <h1 style={styles.title}>Distributed Search Engine</h1>
        <SearchBar onSearch={handleSearch} />
        {loading && <p>Loading...</p>}
        {error && <p style={styles.error}>{error}</p>}
        <div style={styles.results}>
          {results.map((r) => (
            <ResultItem key={r.url} result={r} />
          ))}
        </div>
      </main>
    </>
  );
}

const styles = {
  main: {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '2rem',
    fontFamily: "'Inter', sans-serif",
  },
  title: {
    textAlign: 'center',
    marginBottom: '1.5rem',
    fontSize: '2rem',
    color: '#222',
  },
  results: {
    marginTop: '2rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
  },
  error: {
    color: 'red',
  },
};
