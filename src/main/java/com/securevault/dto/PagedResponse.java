package com.securevault.dto;

import java.util.List;

/**
 * PagedResponse DTO
 *
 * Generic wrapper for paginated API responses.
 * Provides complete pagination metadata for client-side pagination controls.
 *
 * Example JSON Response:
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 10,
 *   "totalElements": 150,
 *   "totalPages": 15,
 *   "first": true,
 *   "last": false
 * }
 */
public class PagedResponse<T> {

    /**
     * List of items for the current page
     */
    private List<T> content;

    /**
     * Current page number (0-indexed)
     */
    private int page;

    /**
     * Number of items per page
     */
    private int size;

    /**
     * Total number of items across all pages
     */
    private long totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * True if this is the first page
     */
    private boolean first;

    /**
     * True if this is the last page
     */
    private boolean last;

    /**
     * True if there is a next page
     */
    private boolean hasNext;

    /**
     * True if there is a previous page
     */
    private boolean hasPrevious;

    // ========== Constructors ==========

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = (page == 0);
        this.last = (page >= totalPages - 1);
        this.hasNext = (page < totalPages - 1);
        this.hasPrevious = (page > 0);
    }

    // ========== Getters and Setters ==========

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
