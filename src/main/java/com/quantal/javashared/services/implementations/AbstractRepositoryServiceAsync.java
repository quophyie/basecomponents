package com.quantal.javashared.services.implementations;

import com.quantal.javashared.exceptions.NotFoundException;
import com.quantal.javashared.objectmapper.NullSkippingOrikaBeanMapper;
import com.quantal.javashared.objectmapper.OrikaBeanMapper;
import com.quantal.javashared.services.interfaces.RepositoryServiceAsync;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Created by dman on 16/04/2017.
 */
public class AbstractRepositoryServiceAsync<ModelT, KeyT extends Serializable> implements RepositoryServiceAsync<ModelT, KeyT> {


    private PagingAndSortingRepository<ModelT, KeyT> repository;
    private NullSkippingOrikaBeanMapper nullSkippingOrikaBeanMapper;
    private OrikaBeanMapper orikaBeanMapper;

    private AbstractRepositoryServiceAsync() {}

    public AbstractRepositoryServiceAsync(PagingAndSortingRepository<ModelT, KeyT> repository,
                                          OrikaBeanMapper orikaBeanMapper,
                                          NullSkippingOrikaBeanMapper nullSkippingOrikaBeanMapper) {

        this.repository = repository;
        this.nullSkippingOrikaBeanMapper = nullSkippingOrikaBeanMapper;
        this.orikaBeanMapper = orikaBeanMapper;
    }

    @Override
    public CompletableFuture<ModelT> create(ModelT model) {

        if (!ObjectUtils.allNotNull(model)){
            CompletableFuture exceptionFuture = new CompletableFuture();
            Type modelType = getModelType();
            String className = modelType.toString().split(" ")[1];
             exceptionFuture.completeExceptionally(new NullPointerException(String.format("%s cannot be null", className)));
             return exceptionFuture;
        }
        return CompletableFuture.completedFuture(repository.save(model));
    }

    @Override
    public CompletableFuture<ModelT> saveOrUpdate(ModelT model) {

        return CompletableFuture.completedFuture(repository.save(model));
    }

    @Override
    public CompletableFuture<Iterable<ModelT>> findAll() {
        return CompletableFuture.completedFuture(repository.findAll());
    }

    @Override
    public  CompletableFuture<ModelT> findOne(KeyT id) {
        return CompletableFuture.completedFuture(repository.findOne(id));
    }

    @Override
    public CompletableFuture<Void> deleteById(KeyT id) {
        repository.delete(id);
        return null;
    }

    @Override
    public  CompletableFuture update(KeyT id, ModelT updateData, boolean skipNulls) {
        CompletableFuture exceptionFuture = new CompletableFuture();
        if (!ObjectUtils.allNotNull(updateData)) {
            String errMsg = String.format("Update object data cannot be null");
            exceptionFuture.completeExceptionally(new NullPointerException(errMsg));
            return exceptionFuture;
        }

        if (id == null) {
            String errMsg = String.format("id of update object data is null. id cannot be null");
            exceptionFuture.completeExceptionally(new NullPointerException(errMsg));
            return exceptionFuture;
        }


        Type modelType = getModelType();
        String className = modelType.toString().split(" ")[1];
        return this
                .findOne(id)
                .thenApply(modelToUpdate -> {
                    if (modelToUpdate == null) {
                        exceptionFuture.completeExceptionally(new NotFoundException(String.format("%s not found", className)));
                        return exceptionFuture;
                    }

                    if (skipNulls) {
                        nullSkippingOrikaBeanMapper.map(updateData, modelToUpdate);
                    } else {
                        orikaBeanMapper.map(updateData, modelToUpdate);
                    }
                    return this.saveOrUpdate(modelToUpdate);
                });
    }

    @Override
    public CompletableFuture<Void> delete(ModelT model) {
     this.delete(model);
        return null;
    }

    private Type getModelType() {
        Type superClass = getClass().getGenericSuperclass();
        Type modelType = ((ParameterizedType)superClass).getActualTypeArguments()[0];

        return modelType;
    }


}
