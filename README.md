# Tubes1_GreedBot
## Tugas Besar 1 Stima 2022/2023 - JavaBot Galaxio

<p align="center">
    <img src=image.png
</p>

## Kelompok GreedBot
1. Ghazi Akmal Fauzan	(13521058) 
2. Ilham Akbar			(13521068) 
3. Ahmad Ghulam Ilham   (13521118) 

## Implementasi Algoritma Greedy ke dalam Bot Permainan 

Terdapat beberapa algoritma yang dapat digunakan sebagai algoritma bot. Beberapa algoritma yang dapat digunakan adalah diantaranya algoritma brute force, algoritma greedy, algoritma tree traversal, dan lain-lain. Tetapi, semua algoritma tersebut memiliki kelebihan dan kekurangannya masing-masing. Pada algoritma brute force, dibutuhkan waktu dan resource yang banyak untuk mengetes dan menilai setiap kemungkinan command yang dapat terjadi, meskipun hasil paling optimum yang ditemukan merupakan optimum global pada keadaan tersebut. Kemudian, pada algoritma greedy, tidak diperlukan waktu atau resource yang banyak. Tetapi, dibutuhkan teknik heuristik yang didefinisikan oleh logika dan pola pikir masing-masing pemrogram. Teknik heuristik tersebut mungkin belum dapat menghasilkan solusi optimum global dan walaupun solusi tersebut merupakan solusi optimum global, solusi tersebut susah dibuktikan kebenarannya secara matematis. Algoritma berikutnya yang dapat digunakan adalah algoritma tree traversal. Algoritma ini umum digunakan pada bot game, dikarenakan pada algoritma ini terdapat pengecekkan beberapa kasus tertentu untuk menentukan langkah selanjutnya yang diambil. Tetapi, algoritma ini tidak seintuitif algoritma brute force maupun algoritma greedy bagi pemrogram. Oleh karena itu, penulis menggunakan algoritma greedy sebagai algoritma pembentukan bot dikarenakan cukup intuitif serta tidak memerlukan waktu atau resource yang terlalu banyak. Selain itu, terdapat cukup banyak kemungkinan solusi greedy yang dapat dieksplorasi oleh penulis. Meskipun terdapat peluang bahwa penulis tidak dapat menciptakan algoritma greedy yang menghasilkan solusi optimum global, tetapi setidaknya penulis dapat selalu mencapai solusi optimum lokal yang nilainya mendekati solusi optimum global. 

## Requirement dan Instalasi
1. Java version 11
2. IntelIiJ IDEA
3. NodeJS
4. .Net Core 3.1
5. Maven

## Cara Menjalankan Program
1. Pastikan semua requirement telah diinstall
2. Lakukan konfigurasi jumlah bot yang ingin dimainkan pada file JSON ”appsettings.json” dalam folder “runner-publish” dan “engine-publish”
3. Buka terminal baru pada folder runner-publish.
4. Jalankan runner menggunakan perintah “dotnet GameRunner.dll”
5. Buka terminal baru pada folder engine-publish
6. Jalankan engine menggunakan perintah “dotnet Engine.dll”
7. Buka terminal baru pada folder logger-publish
8. Jalankan engine menggunakan perintah “dotnet Logger.dll”
9. Jalankan seluruh bot yang ingin dimainkan
10. Setelah permainan selesai, riwayat permainan akan tersimpan pada 2 file JSON “GameStateLog_{Timestamp}” dalam folder “logger-publish”. Kedua file tersebut diantaranya GameComplete (hasil akhir dari permainan) dan proses dalam permainan tersebut.
 
## Cara Menjalankan Permainan
1. Ekstrak file zip Galaxio dalam folder “visualiser” sesuai dengan OS 
2. Jalankan aplikasi Galaxio lalu Buka menu “Options”
3. Salin path folder “logger-publish” pada “Log Files Location”, lalu “Save”
4. Buka menu “Load”
5. Pilih file JSON yang ingin diload pada “Game Log”, lalu “Start”
6. Pilih start, pause, rewind, dan reset
7. Bermain!!


