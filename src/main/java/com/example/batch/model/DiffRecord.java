package com.example.batch.model;

public class DiffRecord {
    private String id;
    private String col1;
    private String col2;

    // INSERT / UPDATE / DELETE / NONE
    private String diffType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCol1() { return col1; }
    public void setCol1(String col1) { this.col1 = col1; }

    public String getCol2() { return col2; }
    public void setCol2(String col2) { this.col2 = col2; }

    public String getDiffType() { return diffType; }
    public void setDiffType(String diffType) { this.diffType = diffType; }
}
