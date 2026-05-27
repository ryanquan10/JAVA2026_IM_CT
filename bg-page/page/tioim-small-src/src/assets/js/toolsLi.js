
/* 数据处理 */
// 对数据进行深度编码，解决某些因为参数过长或者模板字符串一些特殊符号无法传参的问题
export function dataEncode(data) {
    return encodeURIComponent(JSON.stringify(data));
}

// 数据深度解码
export function dataDecode(encodeStr) {
    return JSON.parse(decodeURIComponent(encodeStr));
}
// 字符串
// 格式化字符串删除开头与结尾出现的多个空格或\n
export function formatStringToSE(str){
    str = str.replace(/^[\n|\s]+/, '');
    str = str.replace(/[\n|\s]+$/, '');
    return str
}



// 将JS对象转为FormData对象
export function objectToFormData(obj, formData) {
    if (!formData) {
        formData = new FormData();
    }
    if (typeof obj != 'object') {
        return
    }
    for (let key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            const value = obj[key];
            if (value && typeof value === 'object' && value instanceof File) {
                formData.append(key, value);
            } else if (value instanceof Date) {
                formData.append(key, value.toISOString());
            } else if (Array.isArray(value)) {
                value.forEach(v => {
                    formData.append(`${key}[]`, v);
                });
            } else if (typeof value === 'object' && !(value instanceof File)) {
                // 递归处理对象
                objectToFormData(value, formData);
            } else {
                formData.append(key, value);
            }
        }
    }

    return formData;
}


/* DOM节点 */
// 获取目标节点类型
export function getNodeType(node) {
    let type;
    switch (node.nodeType) {
        case Node.TEXT_NODE:
            type = 'text';
            break;
        case Node.ELEMENT_NODE:
            let tagName = node.tagName.toLowerCase()
            if(tagName === 'img' || tagName === 'pre'){
                type = tagName;
                break;
            }
        default:
            type = null;
            break;
    }
    return type;
}

/* 文件处理 */
// 将url转换为blob
export function urlToBlob(url) {
    return fetch(url).then((response) => response.blob());
}
// 将url转换为file
export function fetchUrlAndConvertToFile(url, filename) {
    return fetch(url)
        .then(response => response.blob()) // 将响应转换为Blob
        .then(blob => new File([blob], filename)); // 创建File对象
}
// 将file转为本地url
export function fileTolocalUrl(file) {
    return window.URL.createObjectURL(file);
    // let reader = new FileReader();
    // let src = '';
    // reader.onload = function(e) {
    //     src = e.target.result;
    //     console.log('src', src);
    // };
    // return src
}

// 解决对象浅拷贝问题
// 1.JSON.stringify(json)
// 2.Object.assign({}, obj)

/* 时间日期处理 */
/* 
* 获取当前时间日期
*/
export function getFormatTime(date, separator){
    function add0(num){
      return num < 10? '0' + num : num
    }
    if(!date){
        date = new Date();
    }
    let year = date.getFullYear();
    let month = add0(date.getMonth() + 1);
    let day = add0(date.getDay() + 1);
    if(separator){
      return [year, month, day].join('-');
    }
    return + ('' + year + month + day)
  }