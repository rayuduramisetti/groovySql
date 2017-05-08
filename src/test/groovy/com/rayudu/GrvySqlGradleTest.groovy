package com.rayudu

import org.junit.Test

class GrvySqlGradleTest {

    @Test
    void buildSelectSql() {
      println  dSB().select('User') {
          join ('UserPermission', type: 'OUTER') {
              on('User': 'userId', 'UserPermission': 'userId')
              on('User': 'userName', 'UserPermission': 'userName')
          }
          where(['firstName = :firstName', 'lastName = :lastName'])
          where(['userName = :userName'])
          //orderBy(sorting: 'userName+DESC')
          //orderBy(sort: 'userName,firstName')
          orderBy(sort: 'userName+DESC,firstName+ASC')
          //orderBy(sort: 'userName')
          //orderBy(sort: 'userName', order: 'desc')
          pagination(offset: 0, pageSize: 3)
      }.build()
    }

    private dSB() {
        new DynamicSqlBuilder()
    }
}