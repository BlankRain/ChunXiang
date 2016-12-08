# ChunXiang
春香~ 

## Some Code To Get Data From Meituan WebSite

> yeah, It looks like someting in a big-data way though it's really not too big .

> >--By Blank Rain

哈哈

- 代码说明
  在geo_ip.txt里面添加信息,信息从美团上切换地址,然后就可以拿到ID了.  
  
  记录里添加`,pass` 意思是跳过本行
  
  运行后,抓取的数据会保存在`./data/$geo_id` 文件里
  
  抓菜单的时候,会读取`./data/$geo_id`的每行记录,然后去抓,所以,如果抓完了,就把数据转义到其他文件里面
  
  当然,你可以不转,这样会二次抓取.
  
- 其他说明

  没有严格测试
  
  Not Well Tested~ 

- 为啥不一套流程自动化采集呢? 还要手动和程序交互,手动选点,手动整理数据?

  因为我懒啊~ 
