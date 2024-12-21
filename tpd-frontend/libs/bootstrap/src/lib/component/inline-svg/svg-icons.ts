import { Injectable } from '@angular/core';

@Injectable()
export class SvgIcons {
    public readonly Files = {
        pdf: 'svg/files/pdf.svg',
        xml: 'svg/files/xml.svg',
        xlsx: 'svg/files/xlsx.svg',
        xls: 'svg/files/xls.svg',
        doc: 'svg/files/doc.svg',
        zip: 'svg/files/zip.svg',
    };

    public readonly Avatar = {
        blank: './assets/media/svg/avatars/blank.svg',
    };
    public readonly Duotone = {
        abstract: {
            abs010: 'icons/duotune/abstract/abs010.svg',
            cross: 'icons/duotune/abstract/abs011.svg',
            abs015: 'icons/duotune/abstract/abs015.svg',
            abs026: 'icons/duotune/abstract/abs026.svg',
            abs027: 'icons/duotune/abstract/abs027.svg',
            abs037: 'icons/duotune/abstract/abs037.svg',
            abs039: 'icons/duotune/abstract/abs039.svg',
        },
        art: {
            art006: 'icons/duotune/art/art006.svg',
            pencil: 'icons/duotune/art/art005.svg',
        },
        arrows: {
            circleAround: 'icons/duotune/arrows/arr029.svg',
            down: 'icons/duotune/arrows/arr072.svg',
            top: 'icons/duotune/arrows/arr073.svg',
            right: 'icons/duotune/arrows/arr023.svg',
            backLeft: 'icons/duotune/arrows/arr046.svg',
            shortLine: 'icons/duotune/arrows/arr090.svg',
            line: 'icons/duotune/arrows/arr089.svg',
            download: 'icons/duotune/arrows/arr091.svg',
            upload: 'icons/duotune/arrows/arr078.svg',
            chevronRight: 'icons/duotune/arrows/arr071.svg',
            cross: 'icons/duotune/arrows/arr061.svg',
            doubleChevronRight: 'icons/duotune/arrows/arr080.svg',
            arrowLeft: 'icons/duotune/arrows/arr063.svg',
            arrowRight: 'icons/duotune/arrows/arr064.svg',
            inside: 'icons/duotune/arrows/arr076.svg',
        },
        ecommerce: {
            basket: 'icons/duotune/ecommerce/ecm005.svg',
        },
        finance: {
            strongbox: 'icons/duotune/finance/fin005.svg',
        },
        files: {
            folderPlus: 'icons/duotune/files/fil013.svg',
            folderDownload: 'icons/duotune/files/fil017.svg',
            folderUpload: 'icons/duotune/files/fil018.svg',
            folder: 'icons/duotune/files/fil012.svg',
            fileCheck: 'icons/duotune/files/fil008.svg',
            fileCross: 'icons/duotune/files/fil007.svg',
        },
        general: {
            home: 'icons/duotune/general/gen001.svg',
            layerCard: 'icons/duotune/general/gen024.svg',
            bookmark: 'icons/duotune/general/gen056.svg',
            checkCircle: 'icons/duotune/general/gen043.svg',
            calendar: 'icons/duotune/general/gen014.svg',
            crossSquare: 'icons/duotune/general/gen034.svg',
            crossCircle: 'icons/duotune/general/gen040.svg',
            edit: 'icons/duotune/general/gen055.svg',
            histogram: 'icons/duotune/general/gen032.svg',
            markerMap: 'icons/duotune/general/gen018.svg',
            search: 'icons/duotune/general/gen021.svg',
            shieldCheck:'icons/duotune/general/gen048.svg',
            shieldUser: 'icons/duotune/general/gen049.svg',
            shieldLock: 'icons/duotune/general/gen051.svg',
            shieldCross: 'icons/duotune/general/gen050.svg',
            exclamationCircle: 'icons/duotune/general/gen044.svg',
            plusCircle: 'icons/duotune/general/gen041.svg',
            plusSquare: 'icons/duotune/general/gen035.svg',
            minusCircle: 'icons/duotune/general/gen042.svg',
            minusSquare: 'icons/duotune/general/gen036.svg',
            switch: 'icons/duotune/general/gen019.svg',
            trash: 'icons/duotune/general/gen027.svg',
            layerVertical: 'icons/duotune/general/gen009.svg',
            layerHorizontal: 'icons/duotune/general/gen010.svg',
            otherHorizontal: 'icons/duotune/general/gen052.svg',
            otherVertical: 'icons/duotune/general/gen053.svg',
            filter: 'icons/duotune/general/gen031.svg',
            setting: 'icons/duotune/general/gen019.svg',
            send: 'icons/duotune/general/gen016.svg',
            rocket: 'icons/duotune/general/gen002.svg',
            star: 'icons/duotune/general/gen003.svg',
        },
        communication: {
            addressBook: 'icons/duotune/communication/com005.svg',
            envelopOpen: 'icons/duotune/communication/com010.svg',
            envelopClose: 'icons/duotune/communication/com011.svg',
            user: 'icons/duotune/communication/com013.svg',
        },
        coding: {
            setting: 'icons/duotune/coding/setting.svg',
            spinner: 'icons/duotune/coding/spinner.svg',
            link: 'icons/duotune/coding/link.svg',
        },
        tech: {
            fingerprint: 'icons/duotune/technology/teh004.svg',
            tech008: 'icons/duotune/technology/teh008.svg',
        },
        medecine: {
            cardio: 'icons/duotune/medicine/med001.svg',
        },
    };

    public readonly Simple = {
        communication: {
            addressBookCard: 'icons/6/communication/address-card.svg',
            addUser: 'icons/6/communication/add-user.svg',
            call: 'icons/6/communication/call_1.svg',
            callCircle: 'icons/6/communication/call.svg',
            deleteUser: 'icons/6/communication/delete-user.svg',
            dialNumbers: 'icons/6/communication/dial-numbers.svg',
            group: 'icons/6/communication/group.svg',
            phone: 'icons/6/communication/call_1.svg',
            shieldUser: 'icons/6/communication/shield-user.svg',
            incomingBox: 'icons/6/communication/incoming-box.svg',
            send: 'icons/6/communication/send.svg',
            share: 'icons/6/communication/share.svg',
            write: 'icons/6/communication/write.svg',
        },
        devices: {
            diagnostics: 'icons/6/devices/diagnostics.svg',
            server: 'icons/6/devices/server.svg',
            hardDrive: 'icons/6/devices/hard-drive.svg',
        },
        design: {
            difference: 'icons/6/design/difference.svg',
            flatten: 'icons/6/design/flatten.svg',
            position: 'icons/6/design/position.svg',
        },
        files: {
            pdf: 'icons/6/files/pdf.svg',
            xlsx: 'icons/6/files/xlsx.svg',
            folder: 'icons/6/files/folder.svg',
            upload: 'icons/6/files/upload.svg',
            selectedFile: 'icons/6/files/selected-file.svg',
            import: 'icons/6/files/import.svg',
            share: 'icons/6/files/share.svg',
        },
        home: {
            key: 'icons/6/home/key.svg',
            globe: 'icons/6/home/globe.svg',
        },
        map: {
            marker1: 'icons/6/map/marker1.svg',
        },
        general: {
            binocular: 'icons/6/general/binocular.svg',
            heart: 'icons/6/general/heart.svg',
            user: 'icons/6/general/user.svg',
            otherVertical: 'icons/6/general/other1.svg',
            otherHorizontal: 'icons/6/general/other2.svg',
            search: 'icons/6/general/search.svg',
            shieldProtected: 'icons/6/general/shield-protected.svg',
            shieldCheck: 'icons/6/general/shield-check.svg',
            visible: 'icons/6/general/visible.svg',
            hidden: 'icons/6/general/hidden.svg',
            like: 'icons/6/general/like.svg',
        },
        electric: {
            socketEU: 'icons/6/electric/socket-eu.svg',
        },
        code: {
            backspace: 'icons/6/code/backspace.svg',
            doneCircle: 'icons/6/code/done-circle.svg',
            errorCircle: 'icons/6/code/error-circle.svg',
            git4: 'icons/6/code/git4.svg',
            compiling: 'icons/6/code/compiling.svg',
            lockOverturning: 'icons/6/code/lock-overturning.svg',
            lockCircle: 'icons/6/code/lock-circle.svg',
            minusCircle: 'icons/6/code/minus.svg',
            plusCircle: 'icons/6/code/plus.svg',
            terminal: 'icons/6/code/terminal.svg',
            settings4: 'icons/6/code/settings4.svg',
            infoCircle: 'icons/6/code/info-circle.svg',
        },
        navigation: {
            backLeft: 'icons/6/navigation/left-3.svg',
        },
        shopping: {
            box2: 'icons/6/shopping/box2.svg',
            box3: 'icons/6/shopping/box3.svg',
            chartBar1: 'icons/6/shopping/chart-bar1.svg',
            chartBar2: 'icons/6/shopping/chart-bar2.svg',
            chartBar3: 'icons/6/shopping/chart-bar3.svg',
            charLine1: 'icons/6/shopping/chart-line1.svg',
            charLine2: 'icons/6/shopping/chart-line2.svg',
            price1: 'icons/6/shopping/price1.svg',
            price2: 'icons/6/shopping/price2.svg',
            wallet: 'icons/6/shopping/wallet.svg',
            wallet2: 'icons/6/shopping/wallet2.svg',
            wallet3: 'icons/6/shopping/wallet3.svg',
        },
        tools: {
            angleGrinder: 'icons/6/tools/angle-grinder.svg',
            swissKnife: 'icons/6/tools/swiss-knife.svg',
            screwdriver: 'icons/6/tools/screwdriver.svg',
        },
    };

    public getExtension(filename: string): string {
        return filename.slice((Math.max(0, filename.lastIndexOf('.')) || Infinity) + 1);
    }
}

export const mSvgIcons = new SvgIcons();
