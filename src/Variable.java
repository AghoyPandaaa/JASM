class Variable {
    long address;
    long size;
    long length;
    long value;

    public Variable(long address, long size, long length, long value) {
        this.address = address;
        this.size = size;
        this.length = length;
        this.value = value;
    }
}