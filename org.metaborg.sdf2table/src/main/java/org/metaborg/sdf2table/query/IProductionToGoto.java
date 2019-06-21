package org.metaborg.sdf2table.parsetable.query;

public interface IProductionToGoto {

    /*
     * Whether a goto for the given production id is present.
     */
    boolean contains(int productionId);

    /*
     * Get the goto state id for the given production id.
     */
    int get(int productionId);

}
