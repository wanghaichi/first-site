
let createDom = function(dom, parent, attr, classes, styles, content){
    let div = $("<" + dom + "></ " + dom + ">");
    attr.forEach(function(item){
        div.attr(item.key, item.value);
    });
    classes.forEach(function(value){
        div.addClass(value);
    });
    styles.forEach(function(item){
        div.css(item.key, item.value);
    });
    console.log(content);
    div.html(content);
    parent.append(div);
    return div;
};


let map = new Map();

$.getJSON('data/result.json', function (json){
    // 数据排序
    json.sort(function(a, b){
        if(a.a.name.localeCompare(b.a.name) === 0){
            if(a.a.lines[0] - b.a.lines[0] === 0){
                return a.a.lines[1] - b.a.lines[1];
            }
            return a.a.lines[0] - b.a.lines[1];
        }
        return a.a.name.localeCompare(b.a.name);
    });
    let fileName = [];
    $.each(json, function(index, item){
        let a = item.a.name = item.a.name.substr(item.a.name.lastIndexOf('/')+1);
        let b = item.b.name = item.b.name.substr(item.b.name.lastIndexOf('/')+1);
        let s = "";
        if(a.localeCompare(b) < 0){
            s = a + " " + b;
        }
        else{
            s = b + " " + a;
        }
        if(map.has(s)){
            let count = map.get(s);
            count ++;
            map.set(s, count);
        }
        else{
            map.set(s, 1);
        }
        if(fileName.indexOf(a) === -1){
            fileName.push(a);
        }
        if(fileName.indexOf(b) === -1){
            fileName.push(b);
        }
    });
    let filePairs = [];
    for(let i = 0, len = fileName.length; i < len; i ++){
        for(let j = i; j < len; j ++){
            let s = "";
            if(fileName[i].localeCompare(fileName[j]) < 0){
                s = fileName[i] + " " + fileName[j];
            }
            else{
                s = fileName[j] + " " + fileName[i];
            }
            if(map.has(s)){
                let count = map.get(s);
                filePairs.push({
                    'names':    s,
                    'value':    count
                });
            }
        }
    }
    filePairs.sort(function(a, b){
        if(a.value - b.value === 0){
            return a.names.localeCompare(b.names);
        }
        return b.value - a.value;
    });
    $.each(filePairs, function(index, item){
        [s1, s2] = item.names.split(' ');
        let container = $("#container");
        let block = createDom('div', container, [], ['code-name-block'], [], null);
        let codeA = createDom('div', block, [], ['code-name'], [], null);
        createDom('h4', codeA, [], [], [], s1);
        let codeB = createDom('div', block, [], ['code-name'], [], null);
        createDom('h4', codeB, [], [], [], s2);
        let codeContent = createDom('div', block, [], ['code-name-content'], [], null);
        createDom('p', codeContent, [], [], [], "find clone pairs <strong>" + item.value +"</strong>");
        let more = createDom('div', block, [], ['view-more'], [], null);
        createDom('a', more, [{key: 'href', value: '/start/compare.html?a=' + s1 + "&b=" + s2}], [], [], "<p>view more</p>");
    });
});


