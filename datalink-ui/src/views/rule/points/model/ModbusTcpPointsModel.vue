<template>
  <div>
    <div style='width: 100%;padding-bottom: 10px' v-show='!disableEdit'>
      <a-button type='primary' @click='newItem'>添加</a-button>
    </div>

    <a-table
      :columns='columns'
      :dataSource='data'
      :pagination='false'
      :loading='loading'
      size='small'
      style='border: 1px #e8e3e3 solid'
    >
      <template v-for="(col, i) in ['tag', 'func','address','type']" :slot='col' slot-scope='text, record'>
        <a-input
          :key='col'
          v-if='record.editable'
          style='margin: -5px 0'
          :value='text'
          :placeholder='columns[i].title'
          @change='e => handleChange(e.target.value, record.key, col)'
        />
        <template v-else>{{ text }}</template>
      </template>

      <template slot='action' slot-scope='text, record'>
        <template v-if='record.editable'>
                      <span v-if='record.isNew'>
                        <a @click='saveRow(record)'>确定</a>
                        <a-divider type='vertical' />
                        <a-popconfirm title='删除此点位？' @confirm='remove(record.key)'>
                          <a>删除</a>
                        </a-popconfirm>
                      </span>
          <span v-else>
                        <a @click='saveRow(record)'>保存</a>
                        <a-divider type='vertical' />
                        <a @click='cancel(record.key)'>取消</a>
                    </span>
        </template>
        <span v-else>
                      <a @click='toggle(record.key)'>编辑</a>
                      <a-divider type='vertical' />
                      <a-popconfirm title='删除此点位？' @confirm='remove(record.key)'>
                        <a>删除</a>
                      </a-popconfirm>
                    </span>
      </template>
    </a-table>
  </div>
</template>

<script>


export default {
  name: 'OpcPointsModel',
  props: {
    disableEdit: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      data: [],
      columns: [
        {
          title: '标签',
          dataIndex: 'tag',
          key: 'tag',
          width: '20%',
          scopedSlots: { customRender: 'tag' }
        },
        {
          title: '功能',
          dataIndex: 'func',
          key: 'func',
          width: '20%',
          scopedSlots: { customRender: 'func' }
        },
        {
          title: '地址',
          dataIndex: 'address',
          key: 'address',
          width: '20%',
          scopedSlots: { customRender: 'address' }
        },
        {
          title: '数据类型',
          dataIndex: 'type',
          key: 'type',
          width: '20%',
          scopedSlots: { customRender: 'type' }
        }
      ]
    }
  },
  mounted() {
    if (!this.disableEdit) {
      this.columns.push({
        title: '操作',
        key: 'action',
        scopedSlots: { customRender: 'action' }
      })
    }
  },
  methods: {
    set(points) {
      this.data = []
      if (!points) return
      let index = 1
      for (let point of points) {
        this.data.push({
          key: index++,
          tag: point.tag,
          func: point.func,
          address: point.address,
          type: point.type,
          editable: false,
          isNew: false
        })
      }
    },
    get() {
      let result = []
      for (let item of this.data) {
        if (item.isNew) continue
        result.push({
          tag: item.tag,
          func: item.func,
          address: item.address,
          type: item.type
        })
      }
      return result
    },
    newItem() {
      const length = this.data.length
      this.data.push({
        key: length === 0 ? '1' : (parseInt(this.data[length - 1].key) + 1).toString(),
        tag: '',
        func: 'holding-register',
        address: '1',
        type: 'INT',
        editable: true,
        isNew: true
      })
    },
    remove(key) {
      this.data = this.data.filter(item => item.key !== key)
    },
    saveRow(record) {
      this.loading = true
      const { key, tag, func, address, type } = record
      if (!tag || !func || !address || !type) {
        this.loading = false
        this.$message.error('填写不完整')
        return
      }
      // if ('GET' !== type && 'WALK' !== type) {
      //   this.loading = false
      //   this.$message.error('读取方式必须是GET或WALK')
      //   return
      // }
      const target = this.data.find(item => item.key === key)
      target.editable = false
      target.isNew = false
      this.loading = false
    },
    toggle(key) {
      const target = this.data.find(item => item.key === key)
      target._originalData = { ...target }
      target.editable = !target.editable
    },
    cancel(key) {
      const target = this.data.find(item => item.key === key)
      Object.keys(target).forEach(key => {
        target[key] = target._originalData[key]
      })
      target._originalData = undefined
    },
    handleChange(value, key, column) {
      const newData = [...this.data]
      const target = newData.find(item => key === item.key)
      if (target) {
        target[column] = value
        this.data = newData
      }
    }

  }
}
</script>

