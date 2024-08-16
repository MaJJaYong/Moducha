package com.example.restea.oauth2.util;

import java.security.SecureRandom;

public class NicknameCreator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] ADVERBS = {
            "기깔나게", "맛나게", "멋지게", "신나게", "재밌게", "부드럽게", "상큼하게", "편안하게", "깨끗하게", "시원하게",
            "밝게", "화사하게", "귀엽게", "섬세하게", "달콤하게", "상쾌하게", "담백하게", "부드럽게", "활발하게", "뜨겁게",
            "조용히", "맛있게", "활기차게", "매끄럽게", "힘차게", "기분좋게", "빠르게", "깔끔하게", "진지하게", "고요하게",
            "예쁘게", "시끄럽게", "깊게", "세련되게", "따뜻하게", "우아하게", "풍성하게", "부드럽게", "편리하게", "즐겁게",
            "신속하게", "안전하게", "차분하게", "감미롭게", "정성껏", "자유롭게", "신중하게", "기쁘게", "능숙하게", "예리하게"
    };
    private static final String[] ADJECTIVES = {
            "차마시는", "달리는", "매운", "빠른", "화려한", "부드러운", "시원한", "따뜻한", "편안한", "쾌적한",
            "상큼한", "귀여운", "빛나는", "달콤한", "기분좋은", "조용한", "묵직한", "상쾌한", "부드러운", "멋진",
            "기운찬", "힘찬", "정성스러운", "예쁜", "멋있는", "풍성한", "깨끗한", "우아한", "고요한", "신선한",
            "세련된", "편리한", "감미로운", "산뜻한", "고급스러운", "여유로운", "능숙한", "신속한", "기쁨을주는", "신중한",
            "무거운", "날카로운", "감동적인", "풍부한", "정확한", "솔직한", "예리한", "탁월한", "자유로운", "사람다운"
    };
    private static final String[] CUTE_ANIMALS = {
            "토끼", "강아지", "고양이", "햄스터", "기니피그", "해피", "펭귄", "코알라", "다람쥐", "귀여운곰",
            "팬더", "여우", "너구리", "오리", "물개", "미니돼지", "슬로스", "고슴도치", "아기다람쥐", "토끼인형",
            "사자새끼", "여우원숭이", "미어캣", "너구리", "이구아나", "쥐", "무당벌레", "토끼털", "다람쥐쥐", "참새",
            "레서판다", "하마", "해달", "원숭이", "수달", "자이언트판다", "오소리", "빙어", "미어캣", "닭",
            "바다사자", "털보", "아기코끼리", "망아지", "스컹크", "수염고양이", "양", "고래", "돌고래", "개구리"
    };

    private static final int MAX_NUMBER = 100;

    private static String getRandomElement(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    private static int getRandomNumber() {
        return RANDOM.nextInt(MAX_NUMBER) + 1;
    }

    public static String getNickname() {
        StringBuilder nickname = new StringBuilder();

        nickname.append(getRandomElement(ADVERBS)).append(" ");
        nickname.append(getRandomElement(ADJECTIVES)).append(" ");
        nickname.append(getRandomElement(CUTE_ANIMALS));
        nickname.append(getRandomNumber());

        return nickname.toString();
    }
}
