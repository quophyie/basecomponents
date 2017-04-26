package com.quantal.shared.services.implementations;

import com.quantal.shared.exceptions.NotFoundException;
import com.quantal.shared.objectmapper.NullSkippingOrikaBeanMapper;
import com.quantal.shared.objectmapper.OrikaBeanMapper;
import com.quantal.shared.services.interfaces.RepositoryService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by dman on 16/04/2017.
 */
public class AbstractRepositoryService<ModelT, KeyT extends Serializable> implements RepositoryService<ModelT, KeyT> {


    private PagingAndSortingRepository<ModelT, KeyT> repository;
    private NullSkippingOrikaBeanMapper nullSkippingOrikaBeanMapper;
    private OrikaBeanMapper orikaBeanMapper;

    private AbstractRepositoryService() {}

    public AbstractRepositoryService(PagingAndSortingRepository<ModelT, KeyT> repository,
                                     OrikaBeanMapper orikaBeanMapper,
                                     NullSkippingOrikaBeanMapper nullSkippingOrikaBeanMapper) {

        this.repository = repository;
        this.nullSkippingOrikaBeanMapper = nullSkippingOrikaBeanMapper;
        this.orikaBeanMapper = orikaBeanMapper;
    }

    @Override
    public ModelT create(ModelT model) {

        if (!ObjectUtils.allNotNull(model)){
            Type modelType = getModelType();
            String className = modelType.toString().split(" ")[1];
            throw new NullPointerException(String.format("%s cannot be null", className));
        }
        return repository.save(model);
    }

    @Override
    public ModelT saveOrUpdate(ModelT model) {
        return repository.save(model);
    }

    @Override
    public Iterable<ModelT> findAll() {
        return repository.findAll();
    }

    @Override
    public ModelT findOne(KeyT id) {
        return repository.findOne(id);
    }

    @Override
    public void deleteById(KeyT id) {
     repository.delete(id);
    }

    @Override
    public ModelT update(KeyT id, ModelT updateData, boolean skipNulls) {
        if (!ObjectUtils.allNotNull(updateData)){
            String errMsg = String.format("Update object data cannot be null");
            throw new NullPointerException(errMsg);
        }

        if (id == null){
            String errMsg = String.format("id of update object data is null. id cannot be null");
            throw new NullPointerException(errMsg);
        }


        Type modelType = getModelType();
        String className = modelType.toString().split(" ")[1];
        ModelT modelToUpdate = this.findOne(id);

        if (modelToUpdate == null){
            throw new NotFoundException(String.format("%s not found", className));
        }

        if(skipNulls) {
            nullSkippingOrikaBeanMapper.map(updateData, modelToUpdate);
        } else {
            orikaBeanMapper.map(updateData, modelToUpdate);
        }
        return this.saveOrUpdate(modelToUpdate);
    }

    @Override
    public void delete(ModelT model) {
     this.delete(model);
    }

    private Type getModelType() {
        Type superClass = getClass().getGenericSuperclass();
        Type modelType = ((ParameterizedType)superClass).getActualTypeArguments()[0];

        return modelType;
    }


}
