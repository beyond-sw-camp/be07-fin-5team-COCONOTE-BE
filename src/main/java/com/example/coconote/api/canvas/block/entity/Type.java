package com.example.coconote.api.canvas.block.entity;

public enum Type {
    heading, paragraph, orderedList, bulletList, listItem, image
//    실제 html 태그 : h, p, ol, ul , li, img
    /*
    * h : <h1 data-id="zzzzz">내용</h1> or ~h6
    * p : <p>내용</p>
    * ol : <ol><li><p> 내 <br>용 </p></li></ol> <br /> <br class="" / >
    * ul : <ul><li><p>내용</p></li></ul>
    * li : ul이나 ol 내부에만 들어감
    * img : <img src="주소" />
    * */
}
