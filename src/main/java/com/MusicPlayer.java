package com;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.IOException;

public class MusicPlayer {
    public static void main(String[] args) throws IOException, InterruptedException {
        APIhandler a = new APIhandler();
//        String url = a.returnURL(a.search("505")[0].getVideoId());
        MediaPlayerFactory factory = new MediaPlayerFactory();
        MediaPlayer mediaPlayer = factory.mediaPlayers().newMediaPlayer();
        mediaPlayer.media().play("https://rr3---sn-o3o-qxaz.googlevideo.com/videoplayback?expire=1741019848&ei=aIbFZ_DuHbjCssUPuMryuAo&ip=103.151.209.124&id=o-AElAgCK-4W3UHOYQQI3wF7q8Sg8yoPk5oA6xe0hSyXNY&itag=140&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&met=1740998248%2C&mh=vA&mm=31%2C29&mn=sn-o3o-qxaz%2Csn-gwpa-qxaz&ms=au%2Crdu&mv=m&mvi=3&pl=23&rms=au%2Cau&gcr=in&initcwndbps=1978750&bui=AUWDL3y0ZehGjGJb_njXph1FgsIG_mP6ywWY53-Igr6KM9WxEVKxvpWuNOUKJNEO9EoR_uQo7F_Se5TU&spc=RjZbSd3Xi_QDdutnLavzjJBL7w0pSWdRmwxBdMWyqxT996DUCoXbWpLbilXA1VFKlrkQiA&vprv=1&svpuc=1&mime=audio%2Fmp4&ns=VMogxHM5o-_7zNCcrgOhydwQ&rqh=1&gir=yes&clen=4106201&dur=253.586&lmt=1695837489044707&mt=1740997748&fvip=3&keepalive=yes&lmw=1&fexp=51326932&c=TVHTML5&sefc=1&txp=4532434&n=1gPOslbMs9JI8Q&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cbui%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Cns%2Crqh%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=met%2Cmh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Crms%2Cinitcwndbps&lsig=AFVRHeAwRQIhAOqZeGTPJUeRj_3STKzlUi-dQdpJhHJBOWd14YtvlTQpAiAOe0EC6XIY51g4_QqCYzhf2FlPoEyuX_HDwAC8GeHQww%3D%3D&sig=AJfQdSswRgIhAJHZC543cGiMF3WhKpMTcCO6W8Gam5NuXrKgN8WOdeiKAiEAlIUEE57sqrUnOIqRTR1hyqpI5JX_EVp2rKTtJbCC3j8%3D");

    }
}
