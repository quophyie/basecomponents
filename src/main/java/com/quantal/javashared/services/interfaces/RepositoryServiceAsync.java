package com.quantal.javashared.services.interfaces;

import java.util.concurrent.CompletableFuture;

/**
 * Created by dman on 16/04/2017.
 */
public interface RepositoryServiceAsync< ModelT, KeyT> {

    CompletableFuture<ModelT> create(ModelT model);
    CompletableFuture<ModelT> saveOrUpdate(ModelT model);
    CompletableFuture<Iterable<ModelT>> findAll();
    CompletableFuture<ModelT> findOne(KeyT id);
    CompletableFuture<Void> deleteById(KeyT id);
    CompletableFuture<ModelT> update(KeyT id, ModelT updateData, boolean skipNulls);
    CompletableFuture<Void> delete(ModelT model);
}
