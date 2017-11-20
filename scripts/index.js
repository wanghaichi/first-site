
$(function(){
    let getUrlParam = function(name) {
        //构造一个含有目标参数的正则表达式对象
        let reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        //匹配目标参数
        let r = window.location.search.substr(1).match(reg);
        //返回参数值
        if(r !== null) {
            return decodeURI(r[2]);
        }
        return null;
    };

    // 高亮div id 生成器
    let count = 1;

    let fileNameA = getUrlParam('a');
    let fileNameB = getUrlParam('b');

    // 为克隆代码对生成高亮遮罩
    let generate = function(pair, color){
        let b = pair[0];
        let e = pair[1];

        // 计算上方距离和高度
        let top = 16 + 24 * (b - 1);
        let height = 24 * (e - b + 1);

        let div = $("<div></div>");
        let id = "clone-pair-" +count;
        div.attr('id', id);
        div.addClass('height-code');
        div.css('top', top + 'px');
        div.css('height', height + 'px');
        div.css('background-color', color);
        count ++;
        return div;
    };

    let generatePair = function(pairs){
        let color = getRandomColor();
        let divs = [];
        $.each(pairs, function(index, item){
            let pair = generate(item.lines, color);
            if(item.name === fileNameA)
                codeA.append(pair);
            else
                codeB.append(pair);
            divs.push(pair);
        });

        for(let i = 0, len = divs.length; i < len; i ++){
            divs[i].mouseover(function(){
                for(let j = 0; j < len; j ++){
                    emphasize(divs[j]);
                }
            });
            divs[i].mouseout(function(){
                for(let j = 0; j < len; j ++){
                    de_emphasize(divs[j]);
                }
            });
            divs[i].click(function(){
                if(len === 2){
                    goTo(divs[1-i]);
                }
            })
        }
    };

    // 悬停强调
    let emphasize = function(object){
        object.animate({'opacity': '0.4'}, 200);
    };

    // 鼠标移除
    let de_emphasize = function(object){
        object.animate({'opacity': '0.2'}, 200);
    };

    // 点击跳转
    let goTo =  function (obj){
        let _targetTop = obj.offset().top;//获取位置
        jQuery("html,body").animate({scrollTop:_targetTop},300);//跳转
    };

    // 随机颜色
    let getRandomColor = function(){
        return  '#' +
            (function(color){
                return (color +=  '0123456789abcdef'[Math.floor(Math.random()*16)])
                && (color.length == 6) ?  color : arguments.callee(color);
            })('');
    };

    let comparePair = function(p1, p2){
        if(p1.name !== p2.name)
            return false;
        let l1 = p1.lines;
        let l2 = p2.lines;
        return Math.abs(l1[0]-l2[0]) <= 2 && Math.abs(l1[1] - l2[1] <= 2);
    };

    // 两个代码块
    let codeA = $("#codeA");
    let codeB = $("#codeB");

    // $.getJSON('data/result.json', function (json){

    let codes = [];
    for(let i = 0; i < 2; i ++){
        $.ajax({
            url: '/start/data/java/' + [fileNameA, fileNameB][i],
            type: 'get',
            async: false,
            success: function(code){
                codes.push(code);
            }
        });
    }
    $("#code-name-a").html(fileNameA);
    $("#code-name-b").html(fileNameB);
    $("#code-content-a").html(codes[0]);
    $("#code-content-b").html(codes[1]);

    $.getJSON('data/result.json', function (json){

        let pairs = [];
        $.each(json, function(index, item){
            let a = item.a.name = item.a.name.substr(item.a.name.lastIndexOf('/')+1);
            let b = item.b.name = item.b.name.substr(item.b.name.lastIndexOf('/')+1);
            if((a === fileNameA && b === fileNameB) || (a === fileNameB && b === fileNameA)){
                if(a === fileNameB){
                    let tmp = item.a;
                    item.a = item.b;
                    item.b = tmp;
                }
                pairs.push(item);
            }
        });
        console.log(pairs);
        pairs.sort(function(a, b){
            if(a.a.lines[0] - b.a.lines[0] === 0){
                return a.a.lines[1] - b.a.lines[1];
            }
            return a.a.lines[0] - b.a.lines[1];
        });
        let vis = new Array(pairs.length);
        for(let i = 0, len = pairs.length; i < len; i ++){
            if(vis[i] === 1)
                continue;
            let cur = [];
            cur.push(pairs[i].a);
            cur.push(pairs[i].b);
            for(let j = i + 1; j < len; j ++){
                if(vis[j] === 1)
                    continue;
                let tmp = [];
                cur.forEach(function(item){
                    if(vis[j] === 1){

                    }
                    else{
                        if(comparePair(item, pairs[j].a)){
                            // console.log([item, pairs[j].a]);
                            tmp.push(pairs[j].b);
                            vis[j] = 1;
                        }
                        else if(comparePair(item, pairs[j].b)){
                            vis[j] = 1;
                            tmp.push(pairs[j].a);
                        }
                    }
                });
                cur = cur.concat(tmp);
            }
            vis[i] = 1;
            cur.sort(function(a, b){
                if(a.name.localeCompare(b.name) === 0){
                    if(a.lines[0] - b.lines[0] === 0)
                        return a.lines[1] - b.lines[1];
                    return a.lines[0] - b.lines[0];
                }
                return a.name.localeCompare(b.name);
            });
            let uniqueCur = [];
            for(let i = 0, len = cur.length; i < len; i ++){
                if(i === 0){
                    uniqueCur.push(cur[i]);
                }
                else if(cur[i].name !==  cur[i-1].name){
                    uniqueCur.push(cur[i]);
                }
                else{
                    if(Math.abs(cur[i].lines[0] - cur[i-1].lines[0]) <= 2 && Math.abs(cur[i].lines[1] - cur[i-1].lines[1] <= 2)){}
                    else{
                        uniqueCur.push(cur[i]);
                    }
                }
            }
            console.log(uniqueCur);
            generatePair(uniqueCur)
        }
    });
});




