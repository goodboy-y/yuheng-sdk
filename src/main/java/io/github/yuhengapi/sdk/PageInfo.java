package io.github.yuhengapi.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageInfo {

    @JsonProperty("currentPage")
    private Integer currentPage;

    @JsonProperty("rowsInPage")
    private Integer rowsInPage;

    @JsonProperty("rowsPerPage")
    private Integer rowsPerPage;

    @JsonProperty("totalRows")
    private Long totalRows;

    @JsonProperty("totalPages")
    private Integer totalPages;

    public PageInfo() {
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getRowsInPage() {
        return rowsInPage;
    }

    public void setRowsInPage(Integer rowsInPage) {
        this.rowsInPage = rowsInPage;
    }

    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public Long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
