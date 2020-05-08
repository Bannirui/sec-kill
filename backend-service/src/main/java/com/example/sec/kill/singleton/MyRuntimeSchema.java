package com.example.sec.kill.singleton;

import com.example.sec.kill.dal.pojo.SecKill;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/7
 * @Description:
 */
public class MyRuntimeSchema {
    private static MyRuntimeSchema ourInstance = new MyRuntimeSchema();

    private RuntimeSchema<SecKill> goodsRuntimeSchema;


    public static MyRuntimeSchema getInstance() {
        return ourInstance;
    }

    private MyRuntimeSchema() {
        RuntimeSchema<SecKill> secKillSchemaVar = RuntimeSchema.createFrom(SecKill.class);
        setGoodsRuntimeSchema(secKillSchemaVar);
    }

    public RuntimeSchema<SecKill> getGoodsRuntimeSchema() {
        return goodsRuntimeSchema;
    }

    private void setGoodsRuntimeSchema(RuntimeSchema<SecKill> goodsRuntimeSchema) {
        this.goodsRuntimeSchema = goodsRuntimeSchema;
    }
}
