package com.rayudu

/**
 * Created by Ramisetti on 5/3/17.
 */
public class DynamicSqlBuilder extends FactoryBuilderSupport {

    {
        registerFactory('select', new SelectFactory())
        registerFactory('where', new WhereFactory())
    }
}

class Select {

    String table
    List clauses = []

    String build() {
        String sql = "SELECT * FROM $table"
        clauses.each { Where where ->
            where.clauses.each {
                sql += it
            }
        }
        sql
    }
}

class Where {
    List<String> clauses = []
}

class SelectFactory extends AbstractFactory {

    @Override
    Object newInstance(FactoryBuilderSupport factoryBuilderSupport, name, value, Map attributes) {
        new Select(table: value)
    }

    public void setChild(FactoryBuilderSupport factoryBuilderSupport, Object parent, Object child) {
        if (child instanceof Where) {
            parent.clauses << child
        }
    }
}


class WhereFactory extends AbstractFactory {

    @Override
    Object newInstance(FactoryBuilderSupport factoryBuilderSupport, name, value, Map properties) {
        new Where(clauses: value)
    }

    boolean isLeaf() { true }
}


