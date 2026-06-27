package com.sumika.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TransactionJpaRepository
    extends JpaRepository<TransactionJpaEntity, Long>,
        JpaSpecificationExecutor<TransactionJpaEntity> {}
