import { describe, it, expect } from 'vitest';
import { formatBytes, formatDate, getFiltered, prettyJson } from '../utils';
import { FileSummary, MarkerFilter } from '../types';

describe('formatBytes', () => {
  it('should return "n/a" for null or undefined', () => {
    expect(formatBytes(null as any)).toBe('n/a');
    expect(formatBytes(undefined)).toBe('n/a');
  });

  it('should format bytes correctly', () => {
    expect(formatBytes(500)).toBe('500 B');
    expect(formatBytes(1024)).toBe('1.0 KB');
    expect(formatBytes(1536)).toBe('1.5 KB');
    expect(formatBytes(1048576)).toBe('1.0 MB');
    expect(formatBytes(1572864)).toBe('1.5 MB');
  });
});

describe('formatDate', () => {
  it('should return "n/a" for null or undefined', () => {
    expect(formatDate(null as any)).toBe('n/a');
    expect(formatDate(undefined)).toBe('n/a');
  });

  it('should format valid ISO date', () => {
    const result = formatDate('2025-12-31T23:59:59Z');
    expect(result).toBeTruthy();
    expect(typeof result).toBe('string');
    expect(result).not.toBe('n/a');
  });
});

describe('getFiltered', () => {
  const mockFiles: FileSummary[] = [
    {
      id: 1,
      filename: 'file1.pdf',
      markers: ['URGENT', 'REVIEW'],
      ocrStatus: 'DONE',
    } as FileSummary,
    {
      id: 2,
      filename: 'file2.pdf',
      markers: ['MISSING_INFO'],
      ocrStatus: 'PENDING',
    } as FileSummary,
    {
      id: 3,
      filename: 'file3.pdf',
      markers: [],
      ocrStatus: 'NONE',
    } as FileSummary,
  ];

  it('should return all files when filter is ALL', () => {
    const result = getFiltered(mockFiles, 'ALL', 'ALL');
    expect(result).toHaveLength(3);
  });

  it('should filter by specific marker', () => {
    const result = getFiltered(mockFiles, 'URGENT', 'ALL');
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);
  });

  it('should filter by OCR status', () => {
    const result = getFiltered(mockFiles, 'ALL', 'DONE');
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);
  });

  it('should filter by both marker and OCR status', () => {
    const result = getFiltered(mockFiles, 'URGENT', 'DONE');
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);

    const result2 = getFiltered(mockFiles, 'URGENT', 'PENDING');
    expect(result2).toHaveLength(0);
  });

  it('should filter NEEDS_ATTENTION for files with attention markers', () => {
    const result = getFiltered(mockFiles, 'NEEDS_ATTENTION', 'ALL');
    // URGENT and MISSING_INFO are in NEEDS_ATTENTION_MARKERS
    expect(result.length).toBeGreaterThan(0);
  });
});

describe('prettyJson', () => {
  it('should return "–" for null or undefined', () => {
    expect(prettyJson(null as any)).toBe('–');
    expect(prettyJson(undefined)).toBe('–');
  });

  it('should pretty print valid JSON', () => {
    const json = '{"name":"test","value":123}';
    const result = prettyJson(json);
    expect(result).toContain('\n');
    expect(result).toContain('"name"');
    expect(result).toContain('"test"');
  });

  it('should return original string for invalid JSON', () => {
    const invalid = 'not json';
    expect(prettyJson(invalid)).toBe(invalid);
  });
});
