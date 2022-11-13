package com.programmers.commandline.domain.voucher.repository.impl;

import com.programmers.commandline.domain.voucher.entity.Voucher;
import com.programmers.commandline.domain.voucher.entity.VoucherType;
import com.programmers.commandline.domain.voucher.repository.VoucherRepository;
import com.programmers.commandline.global.aop.LogAspect;
import com.programmers.commandline.global.io.Message;
import com.programmers.commandline.global.util.Verification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Profile("prod")
public class FileVoucherRepository implements VoucherRepository {

    private final String filePath;
    private final File file;

    FileVoucherRepository(@Value("${file.voucherResourcesPath}") String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    @Override
    public Voucher save(Voucher voucher) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, file.exists());

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Id: " + voucher.getVoucherId()
                    + " Type: " + voucher.getType()
                    + " Discount: " + voucher.getDiscount()
                    + " "
                    + voucher.getAmountUnit()
            );

            bufferedWriter.newLine();

            bufferedWriter.close();

            return voucher;
        } catch (IOException e) {
            LogAspect.getLogger().error("FileVoucherRepository save 에러발생");

            throw new RuntimeException(Message.FILE_SAVE_ERROR.getMessage(), e);
        }
    }

    @Override
    public List<Voucher> findAll() {
        List<Voucher> voucherList = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(this.file);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            System.out.println("여기");
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] splitLine = line.split(" ");

                String uuid = splitLine[1];

                VoucherType voucherType = createVoucherType(splitLine[3]);

                Long discount = toLong(splitLine[5]);

                Voucher voucher = voucherType.createVoucher(UUID.fromString(uuid), discount);

                voucherList.add(voucher);
            }

            return voucherList;

        } catch (IOException e) {
            LogAspect.getLogger().error("FileVoucherRepository findAll 에러발생");

            throw new RuntimeException(Message.FILE_READ_ERROR.getMessage());
        } catch (RuntimeException e) {
            LogAspect.getLogger().error("FileVoucherRepository findAll 에러발생");

            throw new RuntimeException(Message.FILE_READ_ERROR.getMessage());
        }
    }

    public VoucherType createVoucherType(String line) {
        return VoucherType.valueOf(line);
    }

    public Long toLong(String discount) {
        Verification.validateParseToNumber(discount);

        return Long.parseLong(discount);
    }
}