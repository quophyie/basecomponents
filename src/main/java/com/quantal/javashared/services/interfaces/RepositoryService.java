package com.quantal.javashared.services.interfaces;

/**
 * Created by dman on 16/04/2017.
 */
public interface RepositoryService< ModelT, KeyT> {

    ModelT create(ModelT model);
    ModelT saveOrUpdate(ModelT model);
    Iterable<ModelT> findAll();
    ModelT findOne(KeyT id);
    void deleteById(KeyT id);
    ModelT update(KeyT id, ModelT updateData, boolean skipNulls);
    void delete(ModelT model);
}
