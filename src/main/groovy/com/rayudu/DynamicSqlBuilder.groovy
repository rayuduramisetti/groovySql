package com.rayudu

/**
 * Created by Ramisetti on 5/3/17.
 */
class DynamicSqlBuilder extends FactoryBuilderSupport {

    {
        registerFactory('select', new SelectFactory())
        registerFactory('where', new WhereFactory())
        registerFactory('orderBy', new SqlOrderByFactory())
        registerFactory('pagination', new SqlPaginationFactory())
        registerFactory('join', new JoinFactory())
        registerFactory('on', new OnFactory())
    }
}

class Select {

    String table
    List clauses = []
    def orderBy
    def pagination

    List joins = []
    List tables = []
    List on = []

    String build() {
        String sql = "SELECT * FROM $table"
        if (joins) {
            joins.each { Join join ->
                sql += " ${join.type ?: 'INNER'} JOIN ${join.table} ON"
                join.on.eachWithIndex {On on, idx ->
                    if (idx > 0) sql += " AND"
                    sql += " ${on.left} ${on.operator ?: '='} ${on.right}"
                }
            }
        }
        clauses.eachWithIndex { Where where, int index ->
            sql += "${ index == 0 ? ' WHERE' : ' OR' } ("
            where.clauses.eachWithIndex { String clause, int indx ->
                sql += "${ indx == 0 ? '' : ' AND' } $clause"
            }
           sql += ' )'
        }
        if (orderBy) {
            if (orderBy.sort?.contains(',')) {
                sql += " ORDER BY ${orderBy.sort.replaceAll('\\+', ' ')}\n"
            } else if (orderBy.sort) {
                sql += addOrderBy(orderBy)
            } else if (orderBy.sorting) {
                String sorting = orderBy.sorting.replaceAll('\\+', ' ')
                sql += " ORDER BY $sorting\n"
            }
        }
        if (pagination) {
            sql += pagination.pageSize ? " OFFSET ${pagination.offset ?: 0} ROWS FETCH NEXT ${pagination.pageSize ?: 25} ROWS ONLY" : ''
        }
        sql
    }

    private String addOrderBy(SqlOrderBy orderBy) {
        orderBy.sort ? " ORDER BY $orderBy.sort ${orderBy.order?.toUpperCase() ?: 'ASC'}\n" : ''
    }
}

class Where {
    List<String> clauses = []
}

class SqlOrderBy {

    String sort
    String order
    String sorting

    String getOrder() {
        order?.toUpperCase()
    }
}

class SqlPagination {

    Integer offset
    Integer pageSize
}

class Join {

    String table
    String type = 'INNER'
    List on = []
}

class On {
    String left
    String right
    String operator
}

class SelectFactory extends AbstractFactory {

    @Override
    Object newInstance(FactoryBuilderSupport factoryBuilderSupport, name, value, Map attributes) {
        new Select(table: value)
    }

    void setChild(FactoryBuilderSupport factoryBuilderSupport, Object parent, Object child) {
        if (child instanceof Where) {
            parent.clauses << child
        }
        if (child instanceof SqlPagination) {
            parent.pagination = child
        }
        if (child instanceof SqlOrderBy) {
            parent.orderBy = child
        }
        if (child instanceof Join) {
            parent.joins << child
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

class SqlOrderByFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        new SqlOrderBy(sort: attributes.sort, order: attributes.order, sorting: attributes.sorting)
    }

    boolean isLeaf() { true }
}

class SqlPaginationFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        new SqlPagination(offset: attributes.offset, pageSize: attributes.pageSize)
    }

    boolean isLeaf() { true }
}

class JoinFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        new Join(table: value, type: attributes.type)
    }

    void setChild(FactoryBuilderSupport factoryBuilderSupport, Object parent, Object child) {
        super.setChild(factoryBuilderSupport, parent, child)
        if (child instanceof On) {
            parent.on << child
        }
    }

    void setParent(FactoryBuilderSupport factoryBuilderSupport, Object parent, Object child) {
        super.setParent(factoryBuilderSupport, parent, child)
        parent.tables << child.table
    }
}

class OnFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        On on = new On()
        if (value) { on.operator = value }
        attributes.each { k, v ->
            if (!on.left) {
                on.left = "$k.$v"
            } else if (!on.right) {
                on.right = "$k.$v"
            }
        }
        attributes.clear()
        on
    }
}

