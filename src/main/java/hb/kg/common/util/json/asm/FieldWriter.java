package hb.kg.common.util.json.asm;

/**
 * An FieldWriter that generates Java fields in bytecode form.
 */
public final class FieldWriter {
    FieldWriter next;
    /**
     * Access flags of this field.
     */
    private final int access;
    /**
     * The index of the constant pool item that contains the name of this method.
     */
    private final int name;
    /**
     * The index of the constant pool item that contains the descriptor of this
     * field.
     */
    private final int desc;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public FieldWriter(final ClassWriter cw,
                       final int access,
                       final String name,
                       final String desc) {
        if (cw.firstField == null) {
            cw.firstField = this;
        } else {
            cw.lastField.next = this;
        }
        cw.lastField = this;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
    }

    // ------------------------------------------------------------------------
    // Implementation of the FieldVisitor interface
    // ------------------------------------------------------------------------
    public void visitEnd() {}

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------
    /**
     * Returns the size of this field.
     * @return the size of this field.
     */
    int getSize() {
        return 8;
    }

    /**
     * Puts the content of this field into the given byte vector.
     * @param out where the content of this field must be put.
     */
    void put(final ByteVector out) {
        final int mask = 393216; // Opcodes.ACC_DEPRECATED | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE |
                                 // ((access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) /
                                 // (ClassWriter.ACC_SYNTHETIC_ATTRIBUTE / Opcodes.ACC_SYNTHETIC));
        out.putShort(access & ~mask).putShort(name).putShort(desc);
        int attributeCount = 0;
        out.putShort(attributeCount);
    }
}
